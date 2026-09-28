package fr.vinarnt.animu.finder.compose.service

import fr.vinarnt.animu.finder.compose.model.CloudflareClearance
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Stores the per-host Cloudflare clearances and turns them into request headers.
 *
 * [fr.vinarnt.animu.finder.compose.repository.provider.ProviderHttpClient] consults this
 * for every request, so any provider whose host is configured automatically replays its
 * cookie + User-Agent. One captured host also covers its subdomains.
 *
 * When a request is blocked and no clearance is stored, [requestClearance] asks the UI
 * (via [solveRequests]) to open the host in the in-app WebView, which passes the
 * challenge and produces the cookie. Providers call it automatically and retry.
 */
class CloudflareClearanceStore(
    private val settingManager: SettingManager,
) {

    private val solveLock = Mutex()
    private val pendingSolves = mutableMapOf<String, CompletableDeferred<CloudflareClearance?>>()
    private val _solveRequests = MutableSharedFlow<String>(extraBufferCapacity = 8)
    /** Hosts awaiting an interactive WebView solve; observed by the app-level UI host. */
    val solveRequests: SharedFlow<String> = _solveRequests.asSharedFlow()

    fun clearances(): Flow<Map<String, CloudflareClearance>> =
        settingManager.getCloudflareClearances()

    suspend fun clearanceFor(host: String): CloudflareClearance? =
        match(host.lowercase(), settingManager.getCloudflareClearances().first())

    suspend fun save(host: String, cookie: String, userAgent: String) =
        settingManager.saveCloudflareClearance(host, CloudflareClearance(cookie.trim(), userAgent.trim()))

    suspend fun remove(host: String) =
        settingManager.removeCloudflareClearance(host)

    /**
     * The headers Cloudflare expects for [url], or empty when no clearance matches its
     * host. A bare cookie value is normalized to `cf_clearance=<value>`.
     */
    suspend fun headersFor(url: String): Map<String, String> {
        val host = hostOf(url) ?: return emptyMap()
        val clearance = clearanceFor(host) ?: return emptyMap()
        val cookie = clearance.cookie.trim()
        if (cookie.isEmpty()) return emptyMap()

        return buildMap {
            put("Cookie", if ("=" in cookie) cookie else "cf_clearance=$cookie")
            if (clearance.userAgent.isNotBlank()) {
                // Older captures stored the JavaScript-serialized UA (wrapped in quotes).
                // Cloudflare binds the clearance to the exact UA, so normalize defensively.
                put("User-Agent", clearance.userAgent.trim().removeSurrounding("\""))
            }
        }
    }

    /**
     * Returns a clearance for [host], asking the UI to solve the challenge in the in-app
     * WebView if none is stored. Suspends until the solve finishes or [timeoutMs] elapses
     * (null on timeout). Concurrent callers for the same host share one solve.
     */
    suspend fun requestClearance(
        host: String,
        timeoutMs: Long = 90_000,
        force: Boolean = false,
    ): CloudflareClearance? {
        val key = host.lowercase()
        if (!force) clearanceFor(key)?.let { return it }

        var created = false
        val deferred = solveLock.withLock {
            pendingSolves[key] ?: CompletableDeferred<CloudflareClearance?>().also {
                pendingSolves[key] = it
                created = true
            }
        }
        if (created) _solveRequests.tryEmit(key)

        return withTimeoutOrNull(timeoutMs) { deferred.await() }
    }

    /** Called by the UI host when a WebView solve finishes. Persists and unblocks callers. */
    suspend fun completeSolve(host: String, clearance: CloudflareClearance?) {
        if (clearance != null) save(host, clearance.cookie, clearance.userAgent)
        solveLock.withLock { pendingSolves.remove(host.lowercase()) }?.complete(clearance)
    }

    fun hostOf(url: String): String? {
        val lower = url.lowercase()
        val start = lower.indexOf("://")
        if (start < 0) return null
        val rest = lower.substring(start + 3)
        return rest.substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':')
            .takeIf { it.isNotBlank() }
    }

    private fun match(host: String, all: Map<String, CloudflareClearance>): CloudflareClearance? {
        all[host]?.let { return it }
        return all.entries.firstOrNull { (key, _) -> host.endsWith(".$key") }?.value
    }
}

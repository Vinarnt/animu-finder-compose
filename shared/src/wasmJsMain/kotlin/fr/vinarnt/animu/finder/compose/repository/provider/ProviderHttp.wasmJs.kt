package fr.vinarnt.animu.finder.compose.repository.provider

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException

/**
 * Percent-encodes [input] like JS `encodeURIComponent`: everything except the
 * unreserved characters `A-Za-z0-9-_.~` becomes `%XX` (UTF-8). Ktor's
 * [io.ktor.http.encodeURLParameter] keeps RFC 3986 reserved characters (`?&=/`)
 * unescaped, which breaks URLs that embed a full target URL as a query value
 * (the target's own query gets parsed as separate parameters).
 */
fun encodeUrlComponent(input: String): String {
    val hex = "0123456789ABCDEF"
    return buildString(input.length) {
        input.encodeToByteArray().forEach { b ->
            val c = b.toInt().toChar()
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '_' || c == '.' || c == '~') {
                append(c)
            } else {
                append('%')
                append(hex[(b.toInt() shr 4) and 0xF])
                append(hex[b.toInt() and 0xF])
            }
        }
    }
}

actual fun provideProviderHttpClient(clearances: CloudflareClearanceStore): ProviderHttpClient =
    CorsProviderHttpClient(createProviderHttpClient(), CORS_PROXIES, clearances)

class CorsProviderHttpClient(
    client: HttpClient,
    private val corsProxies: List<(String) -> String>,
    cloudflare: CloudflareClearanceStore,
) : ProviderHttpClient(client, cloudflare) {

    private val corsLockedDomains = mutableSetOf<String>()
    private var nextProxyIndex = 0

    override suspend fun perform(
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): HttpResponse {
        if (corsProxies.isEmpty()) return super.perform(url, headers, body)

        val host = runCatching { Url(url).host }.getOrNull()
        if (host != null && host in corsLockedDomains) {
            return performThroughProxies(url, headers, body, null)
        }

        return try {
            super.perform(url, headers, body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpStatusException) {
            throw e
        } catch (e: Throwable) {
            if (host != null) corsLockedDomains += host
            performThroughProxies(url, headers, body, e)
        }
    }

    private suspend fun performThroughProxies(
        url: String,
        headers: Map<String, String>,
        body: String?,
        firstError: Throwable?,
    ): HttpResponse {
        val start = nextProxyIndex
        nextProxyIndex = (nextProxyIndex + 1) % corsProxies.size

        var lastError: Throwable? = firstError
        for (i in corsProxies.indices) {
            val proxy = corsProxies[(start + i) % corsProxies.size]
            try {
                return super.perform(proxy(url), headers, body)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastError = e
            }
        }
        throw lastError ?: Exception("All CORS proxies failed")
    }
}

/**
 * Same-origin proxy served by the dev server (`webpack.config.d/0-media-proxy.js`).
 * Fetching through it does not require CORS at all (the request is same-origin), it
 * runs on the user's machine (residential IP, so anime CDNs do not block it) and it
 * rewrites HLS manifests so relative segment/key URIs resolve correctly.
 */
fun localProxyUrl(url: String): String =
    "${window.location.origin}/proxy?url=${encodeUrlComponent(url)}"

val CORS_PROXIES: List<(String) -> String> = listOf(
    { url -> localProxyUrl(url) },
    { url -> "https://proxy.cors.sh/$url" },
    { url -> "https://proxy.corsfix.com/?url=${encodeUrlComponent(url)}" },
)

/**
 * Proxies for media (video) URLs on the web. The local dev-server proxy is the only
 * reliable one: it is same-origin (no CORS), not blocked by the CDNs (residential
 * IP), and rewrites HLS playlists so relative resolution always works regardless of
 * manifest structure. Third-party path-style proxies could not guarantee that.
 */
val MEDIA_PROXIES: List<(String) -> String> = listOf(
    { url -> localProxyUrl(url) },
)

/**
 * Routes a media URL through a CORS proxy so the browser can load cross-origin
 * video. The proxy is picked deterministically per URL so the whole HLS playlist
 * (manifest + segments) stays on a single proxy.
 */
fun proxyMediaUrl(url: String): String {
    if (MEDIA_PROXIES.isEmpty()) return url
    val index = (url.hashCode() % MEDIA_PROXIES.size + MEDIA_PROXIES.size) % MEDIA_PROXIES.size
    return MEDIA_PROXIES[index](url)
}
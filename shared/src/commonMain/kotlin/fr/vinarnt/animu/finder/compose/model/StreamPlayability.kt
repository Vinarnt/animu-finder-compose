package fr.vinarnt.animu.finder.compose.model

/**
 * Decides which targets can play a given stream URL, so each platform only offers
 * streams that work there.
 *
 * Playability is keyed on the CDN host (and URL shape) because that is what decides
 * whether a stream is reachable from each target's network path: the wasmJs build
 * fetches media through the dev-server CORS proxy, native targets (desktop / Android
 * / iOS) fetch it directly.
 *
 * The domain lists reflect hosts verified as of the last audit (the anime CDNs rotate
 * domains, so re-check the lists when behaviour drifts).
 */
object StreamPlayability {

    /** Hosts that only play on wasmJs (served through the CORS proxy). */
    private val WEB_HOSTS = listOf<String>()

    /** Hosts that only play on native targets (direct-fetch file CDNs; the web proxy is unreliable). */
    private val NATIVE_HOSTS = listOf(
        "mp4upload.com",
        "okcdn.ru",
    )

    /** Hosts verified to stream on every target (HLS via the proxy on web, direct on native). */
    private val ALL_HOSTS = listOf(
        "vmpx.online",
        "vmcld.space",
        "vmbox.space",
        "vivibebe.site",
        "vibevibe.workers.dev",
    )

    /** Hosts currently unreachable everywhere (they answer 403/502 or fail to resolve). */
    private val DEAD_HOSTS = listOf(
        "krussdomi.com",
        "animeparadise.moe",
        "anime-sama.fr",
    )

    /**
     * The platforms [url] can play on. An empty set means the stream should not be
     * offered on any target.
     */
    fun platformsFor(url: String): Set<StreamPlatform> {
        val lower = url.lowercase()
        val host = hostOf(lower)
        // Ephemeral HLS workers serve playlists as `master.txt` on rotating subdomains
        // that 404 within minutes. They are dead on every target.
        if (lower.contains("/hls") && lower.endsWith(".txt")) return emptySet()
        if (host == null) return setOf(StreamPlatform.Web, StreamPlatform.Native)
        if (DEAD_HOSTS.any { host == it || host.endsWith(".$it") }) return emptySet()
        if (WEB_HOSTS.any { host == it || host.endsWith(".$it") }) return setOf(StreamPlatform.Web)
        if (NATIVE_HOSTS.any { host == it || host.endsWith(".$it") }) return setOf(StreamPlatform.Native)
        if (ALL_HOSTS.any { host == it || host.endsWith(".$it") }) return setOf(StreamPlatform.Web, StreamPlatform.Native)
        return setOf(StreamPlatform.Web, StreamPlatform.Native)
    }

    private fun hostOf(url: String): String? {
        val start = url.indexOf("://")
        if (start < 0) return null
        val rest = url.substring(start + 3)
        val host = rest.substringBefore('/').substringBefore('?').substringBefore('#').lowercase()
        return host.substringBefore(':')
    }
}
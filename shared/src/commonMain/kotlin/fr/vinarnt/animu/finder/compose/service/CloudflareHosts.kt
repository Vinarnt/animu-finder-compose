package fr.vinarnt.animu.finder.compose.service

/**
 * Hosts known to sit behind Cloudflare. Only these are auto-solved when a provider hits a
 * 403/503 — solving is an interactive WebView flow, so a non-Cloudflare 403 (some sites
 * just return 403) must not trigger it. Manual capture via Settings → Cloudflare still
 * works for any host.
 */
private val cloudflareProtectedHosts = setOf(
    "nakanime.tv",
)

fun isCloudflareProtected(host: String): Boolean {
    val lower = host.lowercase()
    return cloudflareProtectedHosts.any { lower == it || lower.endsWith(".$it") }
}

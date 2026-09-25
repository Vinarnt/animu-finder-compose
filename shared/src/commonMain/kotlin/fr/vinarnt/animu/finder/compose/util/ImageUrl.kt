package fr.vinarnt.animu.finder.compose.util

/**
 * Rewrites an image URL so it can be fetched on the current platform.
 *
 * On the web, cross-origin CDNs that do not send CORS headers (e.g. the Crunchyroll
 * episode thumbnails served by the Tenrai API) are routed through the local
 * dev-server proxy. Native targets return the URL unchanged.
 */
expect fun platformImageUrl(url: String?): String?

package fr.vinarnt.animu.finder.compose.util

import fr.vinarnt.animu.finder.compose.repository.provider.localProxyUrl

/**
 * Episode thumbnails come from CDNs (Crunchyroll) that do not send CORS headers, so
 * the browser cannot read them directly. Same-origin dev-server proxy fixes that.
 */
actual fun platformImageUrl(url: String?): String? =
    url?.let { localProxyUrl(it) }

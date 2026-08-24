package fr.vinarnt.animu.finder.compose.repository.provider

object EmbedResolver {

    /**
     * Returns a directly playable stream URL (m3u8/mp4) from an embed page, or null
     * when the host requires JavaScript (voe/streamtape/gn1r5n) and cannot be resolved
     * by a plain HTTP client.
     */
    /**
     * Whether this embed host can be resolved by a plain HTTP client (i.e. the
     * moly/vidmoly family). Hosts that require JavaScript (voe/streamtape/...) return
     * false so callers can skip fetching them entirely.
     */
    fun canResolve(embedUrl: String): Boolean {
        val host = embedUrl.substringAfter("://").substringBefore("/").lowercase()
        return isMolyFamily(host)
    }

    fun extract(embedUrl: String, html: String): String? {
        val host = embedUrl.substringAfter("://").substringBefore("/").lowercase()
        if (!isMolyFamily(host)) return null

        return Regex("""file\s*:\s*["']([^"']+\.m3u8[^"']*)["']""").find(html)?.groupValues?.get(1)
            ?.unescape()
            ?: Regex("""file\s*:\s*["']([^"']+\.mp4[^"']*)["']""").find(html)?.groupValues?.get(1)
                ?.unescape()
            ?: Regex("""<source[^>]+src=["']([^"']+\.(?:m3u8|mp4)[^"']*)["']""")
                .find(html)?.groupValues?.get(1)
                ?.unescape()
            ?: Regex("""https?://[^"'\s<>]+\.(?:m3u8|mp4)(?:\?[^"'\s<>]*)?""")
                .find(html)?.value
                ?.unescape()
    }

    private fun isMolyFamily(host: String): Boolean =
        host.contains("voembed.net") ||
            host.contains("vidmoly") ||
            host.contains("membed") ||
            host.contains("vmnow")

    private fun String.unescape(): String =
        replace("\\/", "/").replace("\\u0026", "&")
}

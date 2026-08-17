package fr.vinarnt.animu.finder.compose.repository.extractor

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType

class AnimeSamaExtractor(http: ExtractorHttpClient) : BaseExtractor(http) {

    override val providerId = "animesama"
    override val providerName = "Anime-Sama"

    private val base = "https://anime-sama.to"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        val slug = resolveSlug(query) ?: return emptyList()
        val streamUrl = loadStreamUrl(slug, query.episode.absolute) ?: return emptyList()
        return listOf(
            StreamSource(
                providerId = providerId,
                url = streamUrl,
                quality = null,
                isM3U8 = streamUrl.contains(".m3u8"),
                headers = mapOf("Referer" to "$base/"),
                dub = null,
                subtitles = listOf(SubtitleTrack("fr", SubtitleType.Hard)),
                matchScore = 1f,
            )
        )
    }

    private suspend fun resolveSlug(query: EpisodeSearchQuery): String? {
        // Anime-Sama search is a POST to fetch.php returning HTML result links.
        val html = http.postForm(
            "$base/template-php/defaut/fetch.php",
            mapOf("query" to query.animeTitle),
            headers(),
        )

        val regex = Regex(
            """<a[^>]*href="[^"]*/catalogue/([a-z0-9-]+)"[^>]*>(.*?)</a>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        var best: Pair<String, Float>? = null
        for (match in regex.findAll(html)) {
            val slug = match.groupValues[1]
            val title = Regex("<[^>]+>").replace(match.groupValues[2], "").trim()
            val score = bestTitleScore(query, title.ifBlank { slug })
            if (best == null || score > best.second) best = slug to score
        }
        return best?.first
    }

    private suspend fun loadStreamUrl(slug: String, episodeNumber: Int): String? {
        return try {
            val playerUrl = "$base/catalogue/$slug/saison1/vostfr/"
            val html = http.getText(playerUrl, headers())

            // Direct playable URL embedded in the page.
            Regex("""["'](https?://[^"']+\.(?:m3u8|mp4)(?:\?[^"']*)?)["']""")
                .find(html)?.groupValues?.get(1)
                ?: extractFromEpisodeArrays(html, episodeNumber)
        } catch (e: Exception) {
            Logger.w("AnimeSama source extraction failed: ${e.message}", e)
            null
        }
    }

    private fun extractFromEpisodeArrays(html: String, episodeNumber: Int): String? {
        val epsMatch = Regex("""eps\d+\s*=\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            .findAll(html)
            .firstOrNull()
            ?: return null
        val urls = Regex("""["'](https?://[^"']+)["']""")
            .findAll(epsMatch.groupValues[1])
            .map { it.groupValues[1] }
            .toList()
        return urls.getOrNull(episodeNumber - 1)
    }

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
        "X-Requested-With" to "XMLHttpRequest",
    )
}

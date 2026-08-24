package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType

class AnimeSamaProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "animesama"
    override val providerName = "Anime-Sama"

    private val base = "https://anime-sama.to"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("AnimeSama: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val slug = resolveSlug(query) ?: return emptyList()
        Logger.i("AnimeSama: resolved slug '$slug'")
        val streamUrl = loadStreamUrl(slug, query.episode.absolute) ?: return emptyList()
        Logger.i("AnimeSama: resolved stream $streamUrl")
        return listOf(
            StreamSource(
                providerId = providerId,
                url = streamUrl,
                quality = null,
                isM3U8 = streamUrl.contains(".m3u8"),
                headers = mapOf("Referer" to refererFor(streamUrl)),
                dub = null,
                subtitles = listOf(SubtitleTrack("fr", SubtitleType.Hard)),
                matchScore = 1f,
            )
        )
    }

    private suspend fun resolveSlug(query: EpisodeSearchQuery): String? {
        Logger.d("AnimeSama: searching '${query.animeTitle}' via fetch.php")
        val html = http.postForm(
            "$base/template-php/defaut/fetch.php",
            mapOf("query" to query.animeTitle),
            headers(),
        )

        // Each result is an <a> containing an <h3> main title and a <p> subtitle
        // with the alternative names (comma separated).
        val resultRegex = Regex(
            """<a[^>]*href="[^"]*/catalogue/([a-z0-9-]+)"[^>]*>[\s\S]*?<h3[^>]*>([^<]*)</h3>[\s\S]*?<p[^>]*>([\s\S]*?)</p>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        var best: Pair<String, Float>? = null
        for (match in resultRegex.findAll(html)) {
            val slug = match.groupValues[1]
            val names = buildList {
                match.groupValues[2].trim().takeIf { it.isNotBlank() }?.let { add(it) }
                match.groupValues[3]
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .let { addAll(it) }
            }
            val score = names.maxOfOrNull { bestTitleScore(query, it) } ?: 0f
            if (best == null || score > best.second) best = slug to score
        }
        Logger.i("AnimeSama: best slug='${best?.first}' score=${best?.second}")
        return best?.first
    }

    private suspend fun loadStreamUrl(slug: String, episodeNumber: Int): String? {
        val playerUrl = "$base/catalogue/$slug/saison1/vostfr/"
        Logger.d("AnimeSama: fetching player page $playerUrl")
        val html = try {
            http.getText(playerUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimeSama player page fetch failed: ${e.message}", e)
            return null
        }

        // Direct playable URL embedded in the page (legacy layout).
        Regex("""["'](https?://[^"']+\.(?:m3u8|mp4)(?:\?[^"']*)?)["']""")
            .find(html)?.groupValues?.get(1)?.let {
                Logger.i("AnimeSama: found direct stream url in page: $it")
                return it
            }

        // Modern layout: episodes are loaded from a relative episodes.js script.
        val episodesJsSrc = Regex("""<script[^>]+src=['"]([^'"]*episodes\.js[^'"]*)['"]""")
            .find(html)?.groupValues?.get(1)
        if (episodesJsSrc == null) {
            Logger.w("AnimeSama: no episodes.js found in player page")
            return null
        }
        val episodesJsUrl = when {
            episodesJsSrc.startsWith("http") -> episodesJsSrc
            episodesJsSrc.startsWith("/") -> base + episodesJsSrc
            else -> playerUrl.substringBeforeLast("/") + "/" + episodesJsSrc
        }
        Logger.d("AnimeSama: fetching episodes data $episodesJsUrl")
        val epsJs = try {
            http.getText(episodesJsUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimeSama episodes.js fetch failed: ${e.message}", e)
            return null
        }

        val epsArrays = Regex("""var\s+(eps\d+)\s*=\s*\[([\s\S]*?)\]""")
            .findAll(epsJs)
            .map { match ->
                match.groupValues[1] to Regex("""['"]([^'"]+)['"]""")
                    .findAll(match.groupValues[2])
                    .map { it.groupValues[1] }
                    .toList()
            }
            .toList()

        if (epsArrays.isEmpty()) {
            Logger.w("AnimeSama: no eps[] arrays found in episodes.js")
            return null
        }

        for ((name, urls) in epsArrays) {
            val episodeUrl = urls.getOrNull(episodeNumber - 1) ?: continue
            Logger.d("AnimeSama: $name episode $episodeNumber -> $episodeUrl")
            val playable = resolvePlayableUrl(episodeUrl) ?: continue
            Logger.i("AnimeSama: resolved playable stream via $name: $playable")
            return playable
        }

        Logger.w("AnimeSama: no playable stream for episode $episodeNumber")
        return null
    }

    private suspend fun resolvePlayableUrl(url: String): String? {
        if (url.contains(".m3u8") || url.contains(".mp4")) return url

        val html = try {
            http.getText(url, headers())
        } catch (e: Exception) {
            Logger.w("AnimeSama: failed to fetch embed $url: ${e.message}")
            return null
        }

        Regex("""https?://[^\s"'<>]+\.(?:m3u8|mp4)(?:\?[^\s"'<>]*)?""").find(html)?.let {
            return it.value
        }
        Regex("""(?:src|file)\s*[:=]\s*['"]([^'"]+\.(?:mp4|m3u8)(?:\?[^'"]*)?)['"]""")
            .find(html)?.let { match ->
                val path = match.groupValues[1]
                if (path.startsWith("http")) return path
                val host = Regex("""https?://[^/]+""").find(url)?.value ?: return null
                return host + (if (path.startsWith("/")) path else "/" + path)
            }
        Logger.w("AnimeSama: could not extract a direct stream from embed $url")
        return null
    }

    private fun refererFor(url: String): String =
        Regex("""https?://[^/]+""").find(url)?.value?.plus("/") ?: "$base/"

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
        "X-Requested-With" to "XMLHttpRequest",
    )
}
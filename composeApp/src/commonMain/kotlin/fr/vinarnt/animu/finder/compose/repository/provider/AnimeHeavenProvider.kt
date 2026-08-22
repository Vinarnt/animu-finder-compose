package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.CancellationException

class AnimeHeavenProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "animeheaven"
    override val providerName = "AnimeHeaven"

    private val base = "https://animeheaven.me"
    private val maxCandidates = 5

    private data class Candidate(val id: String, val title: String, val score: Float)

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("AnimeHeaven: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val candidates = searchCandidates(query)
        if (candidates.isEmpty()) {
            Logger.w("AnimeHeaven: no search results for '${query.animeTitle}'")
            return emptyList()
        }
        for (candidate in candidates) {
            val episodeKey = resolveEpisodeKey(candidate.id, query.episode.absolute) ?: continue
            val streamUrl = loadStreamUrl(episodeKey) ?: continue
            Logger.i("AnimeHeaven: extracted stream via '${candidate.title}' -> $streamUrl")
            return listOf(
                StreamSource(
                    providerId = providerId,
                    url = streamUrl,
                    quality = null,
                    isM3U8 = false,
                    headers = mapOf("Referer" to "$base/"),
                    dub = null,
                    subtitles = listOf(SubtitleTrack("en", SubtitleType.Hard)),
                    matchScore = 1f,
                )
            )
        }
        Logger.w("AnimeHeaven: no candidate hosts episode ${query.episode.absolute}")
        return emptyList()
    }

    private suspend fun searchCandidates(query: EpisodeSearchQuery): List<Candidate> {
        val searchUrl = "$base/search.php?s=${encode(query.animeTitle)}"
        Logger.d("AnimeHeaven: searching '${query.animeTitle}' -> $searchUrl")
        val html = try {
            http.getText(searchUrl, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeHeaven search failed: ${e.message}", e)
            return emptyList()
        }
        Logger.d("AnimeHeaven: search page fetched (length=${html.length})")

        val resultRegex = Regex(
            """<div class='similarimg'>[\s\S]*?<a href='anime\.php\?([a-z0-9]+)'[\s\S]*?alt='([^']*)'""",
        )
        val candidates = resultRegex.findAll(html).mapNotNull { match ->
            val id = match.groupValues[1]
            val title = decodeEntities(match.groupValues[2]).trim()
            val score = bestTitleScore(query, title)
            Candidate(id, title, score)
        }.toList().sortedByDescending { it.score }.take(maxCandidates)
        Logger.i("AnimeHeaven: ${candidates.size} candidate(s), best='${candidates.firstOrNull()?.title}'")
        return candidates
    }

    private suspend fun resolveEpisodeKey(animeId: String, target: Int): String? {
        val animeUrl = "$base/anime.php?$animeId"
        Logger.d("AnimeHeaven: fetching anime page $animeUrl")
        val html = try {
            http.getText(animeUrl, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeHeaven anime page fetch failed: ${e.message}", e)
            return null
        }
        Logger.d("AnimeHeaven: anime page fetched (length=${html.length})")

        val episodeRegex = Regex(
            """onclick='gatea\("([0-9a-f]{32})"\)'[\s\S]*?class=' watch2 bc '>(\d+)</div>""",
        )
        val matches = episodeRegex.findAll(html).toList()
        Logger.d("AnimeHeaven: parsed ${matches.size} episode(s) from anime page")
        for (match in matches) {
            val number = match.groupValues[2].toIntOrNull() ?: continue
            if (number == target) {
                return match.groupValues[1]
            }
        }
        val listed = matches.mapNotNull { it.groupValues[2].toIntOrNull() }
        Logger.w("AnimeHeaven: episode $target not found (${listed.size} listed: ${listed.minOrNull()}..${listed.maxOrNull()})")
        return null
    }

    private suspend fun loadStreamUrl(episodeKey: String): String? {
        Logger.d("AnimeHeaven: fetching gate page with key '$episodeKey'")
        val html = try {
            http.getText("$base/gate.php", keyHeaders(episodeKey))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeHeaven gate page fetch failed: ${e.message}", e)
            return null
        }
        Logger.d("AnimeHeaven: gate page fetched (length=${html.length})")

        val primary = Regex("""src='(https://[a-z]+\.animeheaven\.me/video\.mp4\?[^']+)'""")
            .find(html)?.groupValues?.get(1)
        if (primary != null) return primary

        val anySource = Regex("""(https://[^'"\s]+/video\.mp4\?[^'"\s]+)""")
            .find(html)?.groupValues?.get(1)
        if (anySource != null) {
            Logger.w("AnimeHeaven: no direct CDN source, using fallback $anySource")
            return anySource
        }
        Logger.w("AnimeHeaven: no video.mp4 source found on gate page")
        return null
    }

    private fun decodeEntities(value: String): String {
        var out = value
        out = out.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
        return Regex("""&#(\d+);""").replace(out) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
    }

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
    )

    private fun keyHeaders(episodeKey: String) = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Cookie" to "key=$episodeKey",
        "Referer" to "$base/",
    )
}
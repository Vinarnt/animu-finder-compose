package fr.vinarnt.animu.finder.compose.repository.extractor

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GogoAnimeExtractor(http: ExtractorHttpClient) : BaseExtractor(http) {

    override val providerId = "gogoanime"
    override val providerName = "GogoAnime"

    private val mirrors = listOf("https://gogoanime3.co", "https://anitaku.so")

    @Volatile
    private var activeBase: String? = null

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        val animeId = resolveAnimeId(query) ?: return emptyList()
        val base = currentBase()
        val episodeNumber = query.episode.absolute
        val watchUrl = "$base/$animeId-episode-$episodeNumber"
        val html = http.getText(watchUrl, headers(base))
        return extractStreamsFromWatchPage(base, html)
    }

    private suspend fun resolveAnimeId(query: EpisodeSearchQuery): String? {
        val bases = if (activeBase != null) listOf(activeBase!!) else mirrors
        var lastError: Exception? = null
        for (base in bases) {
            try {
                val text = http.getText(
                    "$base/ajax/search.html?keyword=${encode(query.animeTitle)}",
                    headers(base),
                )
                val root = json.parseToJsonElement(text).jsonObject
                val content = root["content"]?.jsonPrimitive?.contentOrNull ?: return null

                var best: Pair<String, Float>? = null
                val regex = Regex("""/category/([^"']+)""")
                for (match in regex.findAll(content)) {
                    val id = match.groupValues[1]
                    val title = Regex("""title="([^"]+)"""")
                        .find(content.substring(0, minOf(match.range.last + 200, content.length)))
                        ?.groupValues?.get(1)
                        ?: id
                    val score = bestTitleScore(query, title)
                    if (best == null || score > best.second) best = id to score
                }
                if (best != null) {
                    activeBase = base
                    return best.first
                }
                return null
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("GogoAnime: all mirrors unreachable")
    }

    private fun currentBase(): String = activeBase ?: mirrors.first()

    private suspend fun extractStreamsFromWatchPage(base: String, html: String): List<StreamSource> {
        val streams = mutableListOf<StreamSource>()

        Regex("""["'](https?://[^"']+\.m3u8[^"']*)["']""").findAll(html).forEach {
            streams.add(source(base, it.groupValues[1], "m3u8"))
        }
        Regex("""["'](https?://[^"']+\.mp4[^"']*)["']""").findAll(html).forEach {
            streams.add(source(base, it.groupValues[1], "mp4"))
        }

        val iframe = Regex("""<iframe[^>]+src=["']([^"']+)["']""").find(html)?.groupValues?.get(1)
        if (iframe != null && streams.isEmpty()) {
            try {
                val embedHtml = http.getText(iframe, headers(iframe))
                Regex("""["'](https?://[^"']+\.(?:m3u8|mp4)(?:\?[^"']*)?)["']""").findAll(embedHtml).forEach {
                    streams.add(source(base, it.groupValues[1], if (it.groupValues[1].contains(".m3u8")) "m3u8" else "mp4"))
                }
            } catch (e: Exception) {
                Logger.w("GogoAnime embed extraction failed: ${e.message}", e)
            }
        }

        return streams
    }

    private fun source(base: String, url: String, kind: String) = StreamSource(
        providerId = providerId,
        url = url,
        quality = null,
        isM3U8 = kind == "m3u8",
        headers = mapOf("Referer" to "$base/"),
        matchScore = 1f,
    )

    private fun headers(referer: String = mirrors.first()) = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to referer,
    )
}

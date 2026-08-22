package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs

class AnimeParadiseProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "animeparadise"
    override val providerName = "AnimeParadise"

    private val api = "https://api.animeparadise.moe"
    private val streamHost = "https://stream.animeparadise.moe"
    private val site = "https://animeparadise.moe"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("AnimeParadise: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val animeId = resolveAnime(query) ?: return emptyList()
        Logger.i("AnimeParadise: resolved anime id '$animeId'")

        val episodeUid = resolveEpisode(animeId, query.episode.absolute) ?: return emptyList()
        Logger.i("AnimeParadise: resolved episode uid '$episodeUid' for episode ${query.episode.absolute}")

        val streams = loadStreams(animeId, episodeUid)
        Logger.i("AnimeParadise: extracted ${streams.size} stream(s)")
        return streams
    }

    private suspend fun resolveAnime(query: EpisodeSearchQuery): String? {
        val searchUrl = "$api/search?q=${encode(query.animeTitle)}"
        Logger.d("AnimeParadise: searching '${query.animeTitle}' -> $searchUrl")
        val text = try {
            http.getText(searchUrl, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeParadise search failed: ${e.message}", e)
            return null
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("AnimeParadise search response is not valid JSON (length=${text.length})")
            return null
        }
        val data = root["data"]?.jsonArray ?: run {
            Logger.w("AnimeParadise search response has no 'data' array")
            return null
        }

        var best: Pair<String, Float>? = null
        for (item in data) {
            val obj = item.jsonObject
            val id = str(obj, "_id") ?: continue
            val score = candidateScore(query, obj)
            if (best == null || score > best.second) best = id to score
        }
        Logger.i("AnimeParadise: search returned ${data.size} result(s), best='${best?.first}' score=${best?.second}")
        return best?.first
    }

    private fun candidateScore(query: EpisodeSearchQuery, obj: JsonObject): Float {
        val title = str(obj, "title")
        val alt = obj["alternativeTitle"] as? JsonObject
        val altTitle = alt?.let { str(it, "english", "romaji", "native") }
        return listOfNotNull(title, altTitle).maxOfOrNull { bestTitleScore(query, it) } ?: 0f
    }

    private suspend fun resolveEpisode(animeId: String, absolute: Int): String? {
        val url = "$api/anime/$animeId/episode"
        Logger.d("AnimeParadise: fetching episode list -> $url")
        val text = try {
            http.getText(url, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeParadise episode list fetch failed: ${e.message}", e)
            return null
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("AnimeParadise episode response is not valid JSON (length=${text.length})")
            return null
        }
        val data = root["data"]?.jsonArray ?: run {
            Logger.w("AnimeParadise episode response has no 'data' array")
            return null
        }

        var exact: String? = null
        var nearest: Pair<String, Int>? = null
        for (item in data) {
            val obj = item.jsonObject
            val number = str(obj, "number")?.toIntOrNull() ?: continue
            val uid = str(obj, "uid") ?: continue
            if (number == absolute) {
                exact = uid
                break
            }
            if (nearest == null || abs(number - absolute) < abs(nearest.second - absolute)) {
                nearest = uid to number
            }
        }
        val uid = exact ?: nearest?.first
        Logger.i("AnimeParadise: episode $absolute -> uid '$uid'")
        return uid
    }

    private suspend fun loadStreams(animeId: String, episodeUid: String): List<StreamSource> {
        val url = "$api/ep/$episodeUid?origin=$animeId"
        Logger.d("AnimeParadise: fetching stream data -> $url")
        val text = try {
            http.getText(url, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("AnimeParadise stream fetch failed: ${e.message}", e)
            return emptyList()
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("AnimeParadise stream response is not valid JSON (length=${text.length})")
            return emptyList()
        }
        val episode = root["data"]?.jsonObject?.get("episode")?.jsonObject ?: run {
            Logger.w("AnimeParadise stream response has no 'data.episode' object")
            return emptyList()
        }

        val streamLink = str(episode, "streamLink") ?: run {
            Logger.w("AnimeParadise episode has no 'streamLink'")
            return emptyList()
        }
        val streamUrl = "$streamHost/m3u8?url=$streamLink"
        Logger.i("AnimeParadise: stream url -> $streamUrl")

        val subtitles = collectSubtitles(episode)
        Logger.d("AnimeParadise: found ${subtitles.size} subtitle track(s)")

        return listOf(
            StreamSource(
                providerId = providerId,
                url = streamUrl,
                quality = "auto",
                isM3U8 = true,
                headers = mapOf("Referer" to "$site/"),
                subtitles = subtitles,
                matchScore = 1f,
            )
        )
    }

    private fun collectSubtitles(episode: JsonObject): List<SubtitleTrack> {
        val subData = episode["subData"]?.jsonArray ?: return emptyList()
        val result = mutableListOf<SubtitleTrack>()
        for (item in subData) {
            val obj = item.jsonObject
            val src = str(obj, "src") ?: continue
            val label = str(obj, "label")
            val url = if (src.startsWith("http")) src else "$api/stream/file/$src"
            result.add(SubtitleTrack(label, SubtitleType.Soft, url))
        }
        return result
    }

    private fun str(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val value = obj[key] ?: continue
            if (value is JsonPrimitive && value.isString) return value.content
        }
        return null
    }

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$site/",
        "Origin" to site,
    )
}
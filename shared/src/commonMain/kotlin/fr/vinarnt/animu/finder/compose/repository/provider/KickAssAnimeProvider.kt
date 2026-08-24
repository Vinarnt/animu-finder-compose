package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.math.abs

class KickAssAnimeProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "kickassanime"
    override val providerName = "KickAssAnime"

    private val base = "https://kaa.lt"
    private val cdnReferer = "https://krussdomi.com/"
    private val cdnOrigin = "https://krussdomi.com"
    private val manifestBase = "https://hls.krussdomi.com/manifest"
    private val subLang = "ja-JP"
    private val dubLang = "en-US"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("KickAssAnime: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val show = resolveShow(query) ?: run {
            Logger.w("KickAssAnime: no show found for '${query.animeTitle}'")
            return emptyList()
        }
        Logger.i("KickAssAnime: resolved show '${show.first}' locales=${show.second.joinToString()}")

        val locales = show.second
        val subLocale = if (subLang in locales) subLang else locales.firstOrNull() ?: subLang
        val languages = buildList {
            add(subLocale)
            if (dubLang in locales) add(dubLang)
        }

        val streams = mutableListOf<StreamSource>()
        for (lang in languages) {
            val episodeSlug = resolveEpisodeSlug(show.first, lang, query.episode.absolute) ?: continue
            Logger.i("KickAssAnime: lang $lang episode ${query.episode.absolute} -> $episodeSlug")
            streams += loadStreams(show.first, episodeSlug, isDub = lang == dubLang)
        }
        Logger.i("KickAssAnime: extracted ${streams.size} stream(s) for '${query.animeTitle}' ep ${query.episode.absolute}")
        return streams
    }

    private suspend fun resolveShow(query: EpisodeSearchQuery): Pair<String, List<String>>? {
        val body = buildJsonObject { put("query", query.animeTitle) }.toString()
        val text = try {
            http.postJson("$base/api/fsearch", body, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("KickAssAnime search failed: ${e.message}", e)
            return null
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("KickAssAnime search response is not valid JSON (length=${text.length})")
            return null
        }
        val items = root["result"]?.jsonArray ?: run {
            Logger.w("KickAssAnime search response has no 'result' array")
            return null
        }

        var best: Triple<String, Float, List<String>>? = null
        for (item in items) {
            val obj = item.jsonObject
            val slug = str(obj, "slug") ?: continue
            val locales = obj["locales"]?.jsonArray
                ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                ?: emptyList()
            val title = str(obj, "title") ?: ""
            val titleEn = str(obj, "title_en") ?: ""
            val score = maxOf(bestTitleScore(query, title), bestTitleScore(query, titleEn))
            if (best == null || score > best.second) best = Triple(slug, score, locales)
        }
        Logger.i("KickAssAnime: best slug='${best?.first}' score=${best?.second}")
        return best?.let { it.first to it.third }
    }

    private suspend fun resolveEpisodeSlug(slug: String, lang: String, target: Int): String? {
        var page = 1
        var lastPage = 1
        var nearest: Pair<String, Int>? = null
        while (page <= lastPage) {
            val url = "$base/api/show/$slug/episodes?lang=${encode(lang)}&page=$page"
            val text = try {
                http.getText(url, headers())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.w("KickAssAnime episode list failed: ${e.message}", e)
                return null
            }
            val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
                Logger.w("KickAssAnime episode response is not valid JSON (length=${text.length})")
                return null
            }
            lastPage = root["pages"]?.jsonArray
                ?.mapNotNull { (it as? JsonObject)?.get("number")?.jsonPrimitive?.contentOrNull?.toIntOrNull() }
                ?.maxOrNull() ?: page
            val items = root["result"]?.jsonArray ?: return null

            for (item in items) {
                val obj = item.jsonObject
                val number = (obj["episode_number"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: continue
                val epSlug = str(obj, "slug") ?: continue
                val episodeString = str(obj, "episode_string") ?: number.toString()
                val full = "ep-$episodeString-$epSlug"
                if (number == target) return full
                if (nearest == null || abs(number - target) < abs(nearest.second - target)) {
                    nearest = full to number
                }
            }
            page++
        }
        Logger.w("KickAssAnime: episode $target not found for '$slug' lang=$lang")
        return nearest?.takeIf { abs(it.second - target) <= 3 }?.first
    }

    private suspend fun loadStreams(showSlug: String, episodeSlug: String, isDub: Boolean): List<StreamSource> {
        val url = "$base/api/show/$showSlug/episode/$episodeSlug"
        val text = try {
            http.getText(url, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("KickAssAnime episode detail failed: ${e.message}", e)
            return emptyList()
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("KickAssAnime episode detail is not valid JSON (length=${text.length})")
            return emptyList()
        }
        val servers = root["servers"]?.jsonArray ?: run {
            Logger.w("KickAssAnime episode has no 'servers' array")
            return emptyList()
        }

        val streams = mutableListOf<StreamSource>()
        val seen = mutableSetOf<String>()
        for (server in servers) {
            val obj = server.jsonObject
            val rawSrc = str(obj, "src") ?: continue
            val src = normalizeUrl(rawSrc)
            val manifest: String?
            val subtitles: List<SubtitleTrack>
            if (src.contains(".m3u8") && !src.contains("cat-player")) {
                manifest = src
                subtitles = emptyList()
            } else if (src.contains("cat-player")) {
                val id = extractPlayerId(rawSrc)
                val playerHtml = if (id != null) fetchPlayerPage(src) else null
                manifest = extractManifest(playerHtml, id)
                subtitles = playerHtml?.let { parseSubtitles(it) } ?: emptyList()
            } else {
                Logger.d("KickAssAnime: skipping unsupported server ${str(obj, "name")}")
                continue
            }
            if (manifest == null || !manifest.contains(".m3u8")) {
                Logger.d("KickAssAnime: no playable HLS for server ${str(obj, "name")}")
                continue
            }
            if (!seen.add(manifest)) continue
            Logger.i("KickAssAnime: resolved stream -> $manifest (${subtitles.size} sub(s), dub=$isDub)")
            streams.add(
                StreamSource(
                    providerId = providerId,
                    url = manifest,
                    quality = "auto",
                    isM3U8 = true,
                    headers = mapOf(
                        "Referer" to cdnReferer,
                        // The .jpg HLS segments are served from rotating st1.*.xyz CDN
                        // hosts that reject requests without a matching Origin header
                        // (403); the Referer alone is not enough.
                        "Origin" to cdnOrigin,
                    ),
                    dub = if (isDub) "en" else null,
                    subtitles = subtitles,
                    matchScore = 1f,
                )
            )
        }
        return streams
    }

    private suspend fun fetchPlayerPage(playerUrl: String): String? {
        return try {
            http.getText(playerUrl, headers())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("KickAssAnime player fetch failed for $playerUrl: ${e.message}")
            null
        }
    }

    private fun extractManifest(playerHtml: String?, id: String?): String? {
        if (playerHtml != null) {
            val props = extractProps(playerHtml)
            if (props != null) {
                val manifest = unwrap(props["manifest"])
                val raw = (manifest as? JsonPrimitive)?.contentOrNull
                if (raw != null) return normalizeUrl(raw)
            }
        }
        if (id != null && id.matches(Regex("[0-9a-f]{24}"))) return "$manifestBase/$id/master.m3u8"
        return null
    }

    private fun parseSubtitles(html: String): List<SubtitleTrack> {
        val props = extractProps(html) ?: return emptyList()
        val subtitlesEl = unwrap(props["subtitles"]) ?: return emptyList()
        val entries = subtitlesEl as? JsonArray ?: return emptyList()
        val result = mutableListOf<SubtitleTrack>()
        val seen = mutableSetOf<String>()
        for (entry in entries) {
            val obj = unwrap(entry) as? JsonObject ?: continue
            val srcEl = unwrap(obj["src"]) as? JsonPrimitive ?: continue
            val src = srcEl.contentOrNull ?: continue
            if (!src.contains(".vtt") || !seen.add(src)) continue
            val lang = (unwrap(obj["language"]) as? JsonPrimitive)?.contentOrNull ?: "en"
            result.add(SubtitleTrack(lang, SubtitleType.Soft, src))
        }
        return result
    }

    private fun extractProps(html: String): JsonObject? {
        val match = Regex("""props="([^"]+)"""").find(html) ?: return null
        val decoded = decodeEntities(match.groupValues[1])
        return runCatching { json.parseToJsonElement(decoded).jsonObject }.getOrNull()
    }

    private fun unwrap(el: JsonElement?): JsonElement? {
        if (el is JsonArray && el.size == 2) {
            val first = el.firstOrNull()
            if (first is JsonPrimitive && first.contentOrNull?.toIntOrNull() != null) {
                return el[1]
            }
        }
        return el
    }

    private fun extractPlayerId(src: String): String? =
        Regex("""[?&]id=([^&]+)""").find(src)?.groupValues?.get(1)

    private fun normalizeUrl(url: String): String =
        url.replace(Regex("""^(https?)://+"""), "$1://")
            .let { if (it.startsWith("//")) "https:$it" else it }

    private fun decodeEntities(value: String): String {
        var out = value
        for ((entity, ch) in ENTITY_DECODE) out = out.replace(entity, ch)
        return out.replace("&amp;", "&")
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
        "Referer" to "$base/",
        "x-origin" to "kaa.lt",
    )

    private companion object {
        val ENTITY_DECODE = mapOf(
            "&quot;" to "\"",
            "&#34;" to "\"",
            "&#39;" to "'",
            "&#x27;" to "'",
            "&#x2F;" to "/",
            "&#47;" to "/",
            "&#x3D;" to "=",
            "&#x60;" to "`",
            "&lt;" to "<",
            "&gt;" to ">",
        )
    }
}
package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AnimePaheProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "animepahe"
    override val providerName = "AnimePahe"

    private val base = "https://animepahe.pw"
    private val kwikReferer = "https://animepahe.com"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("AnimePahe: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val animeSession = resolveAnime(query) ?: return emptyList()
        val episodeSession = resolveEpisode(animeSession, query.episode.absolute) ?: return emptyList()
        val streams = loadStreams(animeSession, episodeSession)
        Logger.i("AnimePahe: extracted ${streams.size} stream(s) for '${query.animeTitle}' ep ${query.episode.absolute}")
        return streams
    }

    private suspend fun resolveAnime(query: EpisodeSearchQuery): String? {
        val searchUrl = "$base/api?m=search&l=8&q=${encode(query.animeTitle)}"
        Logger.d("AnimePahe: searching '${query.animeTitle}' -> $searchUrl")
        val text = try {
            http.getText(searchUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimePahe search failed: ${e.message}", e)
            return null
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
            Logger.w("AnimePahe search response is not valid JSON (length=${text.length})")
            return null
        }
        val data = root["data"]?.jsonArray ?: run {
            Logger.w("AnimePahe search response has no 'data' array")
            return null
        }

        var best: Pair<String, Float>? = null
        for (item in data) {
            val obj = item.jsonObject
            val session = obj["session"]?.jsonPrimitive?.contentOrNull ?: continue
            val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: continue
            val score = bestTitleScore(query, title)
            if (best == null || score > best.second) best = session to score
        }
        Logger.i("AnimePahe: search returned ${data.size} result(s), best='${best?.first}' score=${best?.second}")
        return best?.first
    }

    private suspend fun resolveEpisode(animeSession: String, absolute: Int): String? {
        var page = 1
        var lastPage = 1
        while (page <= lastPage) {
            val releaseUrl = "$base/api?m=release&id=$animeSession&sort=episode_dsc&page=$page"
            Logger.d("AnimePahe: fetching release page $page for session $animeSession")
            val text = try {
                http.getText(releaseUrl, headers())
            } catch (e: Exception) {
                Logger.w("AnimePahe release failed: ${e.message}", e)
                return null
            }
            val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: run {
                Logger.w("AnimePahe release response is not valid JSON (length=${text.length})")
                return null
            }
            lastPage = root["last_page"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
            val data = root["data"]?.jsonArray ?: return null

            for (item in data) {
                val obj = item.jsonObject
                val episode = obj["episode"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: continue
                val session = obj["session"]?.jsonPrimitive?.contentOrNull ?: continue
                if (episode.toInt() == absolute) {
                    Logger.i("AnimePahe: found episode $absolute -> session $session")
                    return session
                }
            }
            page++
        }
        Logger.w("AnimePahe: episode $absolute not found for session $animeSession")
        return null
    }

    private suspend fun loadStreams(animeSession: String, episodeSession: String): List<StreamSource> {
        val playUrl = "$base/play/$animeSession/$episodeSession"
        Logger.d("AnimePahe: fetching play page $playUrl")
        val text = try {
            http.getText(playUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimePahe play page failed: ${e.message}", e)
            return emptyList()
        }

        val streams = mutableListOf<StreamSource>()
        val buttonRegex = Regex("""<button[^>]*data-src="[^"]+"[^>]*>""")
        val buttons = buttonRegex.findAll(text).toList()
        Logger.d("AnimePahe: play page has ${buttons.size} resolution button(s)")

        for (match in buttons) {
            val button = match.value
            val kwik = Regex("""data-src="([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
                .find(button)?.groupValues?.get(1) ?: continue
            val audio = Regex("""data-audio="([^"]+)"""")
                .find(button)?.groupValues?.get(1) ?: "unknown"
            val resolution = Regex("""data-resolution="([^"]+)"""")
                .find(button)?.groupValues?.get(1)

            Logger.d("AnimePahe: extracting kwik link (audio=$audio, res=$resolution)")
            val directUrl = extractDirect(kwik) ?: continue
            Logger.i("AnimePahe: resolved kwik -> $directUrl (audio=$audio, res=$resolution)")
            val isDub = audio == "eng"
            streams.add(
                StreamSource(
                    providerId = providerId,
                    url = directUrl,
                    quality = resolution,
                    isM3U8 = directUrl.contains(".m3u8"),
                    headers = mapOf("Referer" to "https://kwik.cx/"),
                    dub = if (isDub) "en" else null,
                    subtitles = if (isDub) {
                        emptyList()
                    } else {
                        listOf(SubtitleTrack("en", SubtitleType.Hard))
                    },
                    matchScore = 1f,
                )
            )
        }

        return streams
    }

    private suspend fun extractDirect(kwikLink: String): String? {
        Logger.d("AnimePahe: fetching kwik page $kwikLink")
        val html = try {
            http.getText(kwikLink, kwikHeaders())
        } catch (e: Exception) {
            Logger.w("AnimePahe kwik fetch failed: ${e.message}", e)
            return null
        }

        val packedScript = Regex("""<script[^>]*>([\s\S]*?)</script>""")
            .findAll(html)
            .map { it.groupValues[1] }
            .firstOrNull { it.contains("eval(function") }
        if (packedScript == null) {
            Logger.w("AnimePahe: no packed script found on kwik page (html length=${html.length})")
            return null
        }

        val scriptPart = KwikPacker.substringAfterLast(packedScript, "eval(function(")
        val unpacked = KwikPacker.unpack("eval(function(" + scriptPart)
        if (unpacked == null) {
            Logger.w("AnimePahe: failed to unpack packed script")
            return null
        }
        if (!unpacked.contains("const source='")) {
            Logger.w("AnimePahe: unpacked script has no 'const source=' (unpacked length=${unpacked.length})")
            return null
        }

        val videoUrl = KwikPacker.substringBefore(
            KwikPacker.substringAfter(unpacked, "const source='"),
            "';",
        )
        if (!videoUrl.startsWith("http")) {
            Logger.w("AnimePahe: extracted source is not a URL: $videoUrl")
            return null
        }
        return videoUrl
    }

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Cookie" to "__ddg1_=;__ddg2_=;",
    )

    private fun kwikHeaders() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to kwikReferer,
    )
}

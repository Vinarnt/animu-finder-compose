package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs

class AnimeYaProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "animeya"
    override val providerName = "AnimeYa"

    private val base = "https://animeya.cc"

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        Logger.i("AnimeYa: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val slug = resolveSlug(query) ?: return emptyList()
        Logger.i("AnimeYa: resolved slug '$slug'")

        val infoHtml = try {
            http.getText("$base/watch/$slug", headers())
        } catch (e: Exception) {
            Logger.w("AnimeYa info fetch failed: ${e.message}", e)
            return emptyList()
        }
        Logger.d("AnimeYa: watch page fetched (length=${infoHtml.length})")

        val target = query.episode.absolute
        val episodes = extractEpisodes(infoHtml)
        Logger.d("AnimeYa: parsed ${episodes.size} episode(s) from RSC stream")
        val episodeId = episodes.firstOrNull { it.second == target }?.first
            ?: episodes.minByOrNull { abs(it.second - target) }?.first
            ?: run {
                Logger.w("AnimeYa: no episode found for target $target")
                return emptyList()
            }
        Logger.i("AnimeYa: using episodeId=$episodeId for episode $target")

        val streams = loadSources(episodeId)
        Logger.i("AnimeYa: extracted ${streams.size} stream(s)")
        return streams
    }

    private suspend fun loadSources(episodeId: String): List<StreamSource> {
        val numericId = episodeId.toIntOrNull() ?: return emptyList()
        val input = "{\"0\":{\"json\":$numericId}}"
        val url = "$base/api/trpc/episode.getEpisodeFullById?batch=1&input=${encode(input)}"
        Logger.d("AnimeYa: fetching trpc sources for episode $numericId")

        val text = try {
            http.getText(url, headers())
        } catch (e: Exception) {
            Logger.w("AnimeYa sources fetch failed: ${e.message}", e)
            return emptyList()
        }

        val root = runCatching { json.parseToJsonElement(text).jsonArray }.getOrNull() ?: run {
            Logger.w("AnimeYa: trpc response is not a JSON array (length=${text.length})")
            return emptyList()
        }
        val first = root.firstOrNull()?.jsonObject ?: return emptyList()
        val episodeData = first["result"]?.jsonObject
            ?.get("data")?.jsonObject
            ?.get("json")?.jsonObject
            ?: run {
                Logger.w("AnimeYa: unexpected trpc response shape")
                return emptyList()
            }

        val players = episodeData["players"]?.jsonArray ?: run {
            Logger.w("AnimeYa: episode data has no 'players' array")
            return emptyList()
        }
        val subtitles = collectSubtitles(episodeData)
        Logger.d("AnimeYa: found ${players.size} player(s), ${subtitles.size} subtitle track(s)")

        val streams = mutableListOf<StreamSource>()
        for (player in players) {
            val obj = player.jsonObject
            val playerUrl = str(obj, "url") ?: continue
            val langue = str(obj, "langue")
            val subType = str(obj, "subType")
            val quality = str(obj, "quality")
            val type = str(obj, "type")

            val isM3U8 = playerUrl.contains(".m3u8") || type.equals("hls", ignoreCase = true)
            val streamUrl = if (isM3U8) playerUrl else resolveHls(playerUrl)
            if (streamUrl == null) {
                Logger.d("AnimeYa: could not resolve embed player $playerUrl (type=$type, langue=$langue, subType=$subType)")
                continue
            }
            Logger.i("AnimeYa: resolved player -> $streamUrl (type=$type, langue=$langue, subType=$subType)")

            val playerSubtitles = collectSubtitles(obj)
            streams.add(
                StreamSource(
                    providerId = providerId,
                    url = streamUrl,
                    quality = quality,
                    isM3U8 = streamUrl.contains(".m3u8"),
                    headers = mapOf("Referer" to "$base/"),
                    dub = dubCodeFor(langue, subType),
                    subtitles = (subtitles + playerSubtitles).distinctBy { it.url },
                    matchScore = 1f,
                )
            )
        }
        return streams
    }

    private suspend fun resolveSlug(query: EpisodeSearchQuery): String? {
        val searchUrl = "$base/browser?search=${encode(query.animeTitle)}"
        Logger.d("AnimeYa: searching '${query.animeTitle}' -> $searchUrl")
        val html = try {
            http.getText(searchUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimeYa search failed: ${e.message}", e)
            return null
        }
        Logger.d("AnimeYa: search page fetched (length=${html.length})")

        val slugs = extractWatchSlugs(html)
        Logger.d("AnimeYa: found ${slugs.size} watch slug(s) in search results")
        var best: Pair<String, Float>? = null
        for (slug in slugs) {
            val score = bestTitleScore(query, slug.replace("-", " "))
            if (best == null || score > best.second) best = slug to score
        }
        Logger.i("AnimeYa: best slug='${best?.first}' score=${best?.second}")
        return best?.first
    }

    private fun extractWatchSlugs(html: String): List<String> {
        val slugs = LinkedHashSet<String>()
        for (obj in parseRscStream(html)) {
            deepSearch(obj, predicate = { el ->
                el is JsonObject &&
                    (el["href"] as? JsonPrimitive)?.isString == true &&
                    (el["href"] as JsonPrimitive).content.startsWith("/watch/")
            }).forEach { el ->
                val href = ((el as JsonObject)["href"] as JsonPrimitive).content
                val slug = href.removePrefix("/watch/").substringBefore("?")
                if (slug.isNotBlank()) slugs.add(slug)
            }
        }
        Regex("""href=["'](?:https?://animeya\.cc)?/watch/([^"'/?#]+)["']""")
            .findAll(html)
            .forEach { slugs.add(it.groupValues[1]) }
        return slugs.toList()
    }

    private fun extractEpisodes(html: String): List<Pair<String, Int>> {
        val episodeLists = mutableListOf<JsonArray>()
        for (obj in parseRscStream(html)) {
            deepSearch(obj, predicate = { el ->
                el is JsonArray && el.isNotEmpty() &&
                    (el.firstOrNull() as? JsonObject)?.get("episodeNumber") is JsonPrimitive
            }).forEach { el -> episodeLists.add(el as JsonArray) }
        }
        if (episodeLists.isEmpty()) return emptyList()

        episodeLists.sortByDescending { it.size }
        return episodeLists.first().mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val id = str(obj, "id") ?: return@mapNotNull null
            val number = (obj["episodeNumber"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
                ?: return@mapNotNull null
            id to number
        }
    }

    private fun parseRscStream(html: String): List<JsonElement> {
        val results = mutableListOf<JsonElement>()
        val regex = Regex("""self\.__next_f\.push\(\[(\d+|0),"((?:[^"\\]|\\.)*)"\]\)""")
        for (match in regex.findAll(html)) {
            var raw = match.groupValues[2]
            raw = runCatching {
                json.parseToJsonElement("\"$raw\"").jsonPrimitive.content
            }.getOrElse {
                raw.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\")
            }
            val idx = raw.indexOf(':')
            if (idx == -1) continue
            val value = raw.substring(idx + 1).trim()
            if (value.startsWith("[") || value.startsWith("{")) {
                runCatching { results.add(json.parseToJsonElement(value)) }
            }
        }
        return results
    }

    private fun deepSearch(
        node: JsonElement?,
        predicate: (JsonElement) -> Boolean,
        results: MutableList<JsonElement> = mutableListOf(),
    ): List<JsonElement> {
        if (node == null) return results
        if (predicate(node)) results.add(node)
        when (node) {
            is JsonArray -> for (x in node) deepSearch(x, predicate, results)
            is JsonObject -> for ((_, v) in node) deepSearch(v, predicate, results)
            else -> {}
        }
        return results
    }

    private fun collectSubtitles(node: JsonElement?): List<SubtitleTrack> {
        val result = mutableListOf<SubtitleTrack>()
        val seen = mutableSetOf<String>()

        fun walk(current: JsonElement?, inheritedLang: String?) {
            if (current == null) return
            when (current) {
                is JsonArray -> current.forEach { walk(it, inheritedLang) }
                is JsonObject -> {
                    val url = str(current, "url", "src", "file", "subtitleUrl", "subUrl")
                    if (url != null) {
                        val lang = str(current, "lang", "language", "label") ?: inheritedLang ?: "Subtitles"
                        val label = str(current, "label", "name") ?: lang
                        val key = "$lang|$label|$url".lowercase()
                        if (seen.add(key)) {
                            result.add(SubtitleTrack(label, SubtitleType.Soft, url))
                        }
                    }
                    for (key in listOf("subtitles", "subtitle", "tracks", "captions")) {
                        val child = current[key]
                        if (child != null) {
                            walk(child, str(current, "lang", "language", "label") ?: inheritedLang)
                        }
                    }
                }
                else -> {}
            }
        }

        walk(node, null)
        return result
    }

    private suspend fun resolveHls(pageUrl: String): String? {
        Logger.d("AnimeYa: resolving HLS from embed $pageUrl")
        val html = try {
            http.getText(pageUrl, headers())
        } catch (e: Exception) {
            Logger.w("AnimeYa: embed fetch failed for $pageUrl: ${e.message}")
            return null
        }
        Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(html)?.let {
            return it.value.replace("\\/", "/")
        }

        val candidates = Regex("""(?:iframe|source)[^>]+src=["']([^"']+)["']""")
            .findAll(html)
            .map { it.groupValues[1].replace("\\/", "/") }
            .filter { it.startsWith("http") }
            .distinct()
            .take(6)
            .toList()

        Logger.d("AnimeYa: no direct m3u8 in embed, probing ${candidates.size} candidate(s)")
        for (candidate in candidates) {
            val page = try {
                http.getText(candidate, headers())
            } catch (e: Exception) {
                continue
            }
            Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(page)?.let {
                return it.value.replace("\\/", "/")
            }
        }
        Logger.w("AnimeYa: could not extract m3u8 from embed $pageUrl")
        return null
    }

    private fun dubCodeFor(langue: String?, subType: String?): String? {
        if (!subType.equals("DUB", ignoreCase = true)) return null
        val lc = (langue ?: "").lowercase()
        return if (lc.contains("fr") || lc.contains("vf")) "fr" else "en"
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
        "Origin" to base,
    )
}

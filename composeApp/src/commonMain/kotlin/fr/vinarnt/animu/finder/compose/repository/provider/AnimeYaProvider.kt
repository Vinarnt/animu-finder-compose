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
            val streamUrl = if (isM3U8 || isDirectMediaUrl(playerUrl)) playerUrl else resolveHls(playerUrl)
            if (streamUrl == null) {
                Logger.d("AnimeYa: could not resolve embed player $playerUrl (type=$type, langue=$langue, subType=$subType)")
                continue
            }
            Logger.i("AnimeYa: resolved player -> $streamUrl (type=$type, langue=$langue, subType=$subType)")

            val playerSubtitles = collectSubtitles(obj)
            // CDNs like mp4upload require a Referer from their own origin (animeya.cc is
            // rejected with 403), so derive it from the resolved stream URL.
            val referer = Regex("""^https?://[^/]+""").find(streamUrl)?.value?.plus("/") ?: "$base/"
            streams.add(
                StreamSource(
                    providerId = providerId,
                    url = streamUrl,
                    quality = quality,
                    isM3U8 = streamUrl.contains(".m3u8"),
                    headers = mapOf("Referer" to referer),
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
        // Each episode is pushed as its own RSC chunk shaped like
        // `{"json":{...,"id":337,"episodeNumber":23,...}}`, so look for any object
        // carrying both `id` and `episodeNumber` rather than a single `eps` array.
        val episodes = LinkedHashMap<String, Int>()
        for (obj in parseRscStream(html)) {
            deepSearch(obj, predicate = { el ->
                el is JsonObject &&
                    (el["episodeNumber"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() != null &&
                    (el["id"] as? JsonPrimitive)?.contentOrNull != null
            }).forEach { el ->
                val episode = el as JsonObject
                val id = (episode["id"] as JsonPrimitive).content
                val number = (episode["episodeNumber"] as JsonPrimitive).content.toInt()
                episodes[id] = number
            }
        }
        return episodes.toList()
    }

    private fun parseRscStream(html: String): List<JsonElement> {
        val results = mutableListOf<JsonElement>()
        // Walk the inline RSC chunks manually. A regex like
        // `self.__next_f\.push\(\[(\d+|0),"((?:[^"\\]|\\.)*)"\]\)` recurses once per
        // character matched by the `*` loop, which overflows the stack (StackOverflowError)
        // on large pages (Next.js pushes big payloads in a single chunk).
        val marker = "self.__next_f.push(["
        var index = 0
        while (true) {
            val start = html.indexOf(marker, index)
            if (start == -1) break
            var i = start + marker.length
            // Numeric chunk id: `(\d+|0)`.
            while (i < html.length && html[i] in '0'..'9') i++
            if (i >= html.length || html[i] != ',') {
                index = start + marker.length
                continue
            }
            i++ // skip ','
            if (i >= html.length || html[i] != '"') {
                index = start + marker.length
                continue
            }
            i++ // skip opening '"'
            // Read the JSON-encoded string content verbatim, preserving backslash
            // escapes, up to the first unescaped '"'.
            val content = StringBuilder()
            while (i < html.length) {
                val c = html[i]
                if (c == '"') {
                    i++
                    break
                }
                content.append(c)
                if (c == '\\' && i + 1 < html.length) {
                    content.append(html[i + 1])
                    i++
                }
                i++
            }

            var raw = content.toString()
            raw = runCatching {
                json.parseToJsonElement("\"$raw\"").jsonPrimitive.content
            }.getOrElse {
                raw.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\")
            }
            val idx = raw.indexOf(':')
            if (idx == -1) {
                index = i
                continue
            }
            val value = raw.substring(idx + 1).trim()
            if (value.startsWith("[") || value.startsWith("{")) {
                runCatching { results.add(json.parseToJsonElement(value)) }
            }
            index = i
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

    private fun isDirectMediaUrl(url: String): Boolean =
        Regex("""\.(m3u8|mp4|webm|mkv|m4v|mov|mp3|aac|flac|ogg|mpd)(?:[?#].*)?$""").containsMatchIn(url)

    // Many embed hosts HTML/unicode-escape quotes (`\u0022`, `&quot;`, ...). Without
    // decoding them, URL-matching character classes span the whole page and produce
    // garbage "streams" (and sometimes huge matches), so decode before matching.
    private fun String.decodeEmbedEntities(): String =
        replace("\\u0026", "&")
            .replace("\\u0022", "\"")
            .replace("\\u0027", "'")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("\\/", "/")

    private suspend fun resolveHls(pageUrl: String): String? {
        Logger.d("AnimeYa: resolving HLS from embed $pageUrl")
        val html = try {
            http.getText(pageUrl, headers()).decodeEmbedEntities()
        } catch (e: Exception) {
            Logger.w("AnimeYa: embed fetch failed for $pageUrl: ${e.message}")
            return null
        }
        Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(html)?.let {
            return it.value
        }
        // Some hosts (e.g. mp4upload) expose the direct file in a player config:
        // `player.src({ type: "video/mp4", src: "https://...video.mp4" })`.
        Regex("""(?:src|file)\s*[:=]\s*["'](https?://[^"']+\.(?:m3u8|mp4|webm|mkv)(?:[?#][^"']*)?)["']""")
            .find(html)?.let { return it.groupValues[1] }

        val candidates = Regex("""(?:iframe|source)[^>]+src=["']([^"']+)["']""")
            .findAll(html)
            .map { it.groupValues[1] }
            .filter { it.startsWith("http") }
            .distinct()
            .take(6)
            .toList()

        Logger.d("AnimeYa: no direct media in embed, probing ${candidates.size} candidate(s)")
        for (candidate in candidates) {
            val page = try {
                http.getText(candidate, headers()).decodeEmbedEntities()
            } catch (e: Exception) {
                continue
            }
            Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(page)?.let {
                return it.value
            }
            Regex("""(?:src|file)\s*[:=]\s*["'](https?://[^"']+\.(?:m3u8|mp4|webm|mkv)(?:[?#][^"']*)?)["']""")
                .find(page)?.let { return it.groupValues[1] }
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

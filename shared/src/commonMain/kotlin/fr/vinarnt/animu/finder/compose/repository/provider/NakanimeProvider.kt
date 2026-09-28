package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.io.encoding.Base64

/**
 * Nakanime (nakanime.tv) — a French aggregator that does not host anything itself.
 *
 * Flow:
 *  1. `/api/catalog/search` returns the anime id + slug (XOR-obfuscated).
 *  2. The anime page (`/anime/{id}/{slug}`) carries a `<script id="anime-data">`
 *     JSON blob whose `episodesList[]` gives the internal `episode_id`.
 *  3. `POST /api/sources/anime` returns the third-party embeds for that episode.
 *
 * The JSON APIs are reachable from a plain HTTP client; the anime page is behind a
 * Cloudflare managed challenge. When a clearance has been captured for `nakanime.tv`
 * (Settings → Cloudflare) [ProviderHttpClient] replays it automatically, so this
 * provider needs no cookie handling of its own; otherwise it logs and yields nothing.
 */
class NakanimeProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "nakanime"
    override val providerName = "Nakanime"

    private val base = "https://nakanime.tv"

    private data class AnimeRef(val id: Int, val slug: String, val title: String)

    private data class NakanimeEpisode(
        val id: Int,
        val number: Int,
        val seasonNumber: Int,
        val hasSources: Boolean,
    )

    private data class NakanimeSource(val url: String, val host: String, val language: String)

    private data class Resolved(val url: String, val referer: String, val isM3U8: Boolean)

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> = coroutineScope {
        Logger.i("Nakanime: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")

        val anime = resolveAnime(query)
        if (anime == null) {
            Logger.w("Nakanime: no anime match for '${query.animeTitle}'")
            return@coroutineScope emptyList()
        }

        val episodes = loadEpisodes(anime) ?: return@coroutineScope emptyList()
        val episode = pickEpisode(episodes, query.episode)
        if (episode == null) {
            Logger.w("Nakanime: no episode ${query.episode.absolute} found on '${anime.title}'")
            return@coroutineScope emptyList()
        }
        if (!episode.hasSources) {
            Logger.w("Nakanime: S${episode.seasonNumber}E${episode.number} has no sources yet")
            return@coroutineScope emptyList()
        }

        val sources = loadSources(anime.id, episode.id) ?: return@coroutineScope emptyList()
        Logger.i("Nakanime: ${sources.size} source(s) for '${anime.title}' S${episode.seasonNumber}E${episode.number}")

        sources.map { source ->
            async {
                val resolved = resolveSource(source)
                if (resolved == null) {
                    Logger.w("Nakanime: could not resolve ${source.host} '${source.url}'")
                    return@async null
                }
                Logger.i("Nakanime: resolved ${source.host} -> ${resolved.url} (lang=${source.language})")
                StreamSource(
                    providerId = providerId,
                    url = resolved.url,
                    quality = null,
                    isM3U8 = resolved.isM3U8,
                    headers = mapOf(
                        "Referer" to resolved.referer,
                        "User-Agent" to DEFAULT_USER_AGENT,
                    ),
                    dub = dubLabel(source.language),
                    subtitles = subtitlesFor(source.language),
                    matchScore = 1f,
                )
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun resolveAnime(query: EpisodeSearchQuery): AnimeRef? {
        val path = "/api/catalog/search?q=${encode(query.animeTitle)}&sort=relevance&page=1&per_page=32"
        val bytes = try {
            http.getBytes("$base$path", apiHeaders())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("Nakanime search failed: ${e.message}", e)
            return null
        }

        val decoded = decodeSearch(bytes, path) ?: run {
            Logger.w("Nakanime: could not decode search payload")
            return null
        }
        val data = runCatching { json.parseToJsonElement(decoded).asObj()?.get("data")?.asArr() }.getOrNull()
            ?: return null

        var best: Pair<AnimeRef, Float>? = null
        for (item in data) {
            val o = item.asObj() ?: continue
            val id = o["id"]?.asInt() ?: continue
            val slug = o["slug"]?.asStr() ?: continue
            val title = o["title"]?.asStr() ?: continue
            val score = maxOf(
                bestTitleScore(query, title),
                o["title_original"]?.asStr()?.let { bestTitleScore(query, it) } ?: 0f,
            )
            val ref = AnimeRef(id, slug, title)
            if (best == null || score > best!!.second) best = ref to score
        }
        return best?.also { Logger.i("Nakanime: best match '${it.first.title}' (id=${it.first.id}) score=${it.second}") }?.first
    }

    /**
     * The search key is derived from the request path *including* its query string, so
     * the decode only works if our encoding matches what the server received. Try the
     * common encodings rather than betting on one.
     */
    private fun decodeSearch(bytes: ByteArray, path: String): String? {
        val candidates = listOf(path, path.replace("%20", "+"), path.replace("%20", " "))
        for (candidate in candidates.distinct()) {
            val decoded = NakanimeCodec.decode(bytes, candidate)
            if (decoded.trimStart().startsWith("{")) return decoded
        }
        return null
    }

    private suspend fun loadEpisodes(anime: AnimeRef): List<NakanimeEpisode>? {
        val html = try {
            http.getText("$base/anime/${anime.id}/${anime.slug}", pageHeaders())
        } catch (e: CancellationException) {
            throw e
        } catch (e: CloudflareChallengeException) {
            Logger.w(
                "Nakanime: anime page '${anime.slug}' is behind a Cloudflare challenge; " +
                    "episode ids cannot be read without a browser session"
            )
            return null
        } catch (e: HttpStatusException) {
            Logger.w(
                "Nakanime: anime page '${anime.slug}' returned HTTP ${e.status}" +
                    (if (e.status == 403 || e.status == 503) " (Cloudflare); episode ids need a browser session" else "")
            )
            return null
        } catch (e: Exception) {
            Logger.w("Nakanime anime page failed for ${anime.slug}: ${e.message}", e)
            return null
        }

        val script = Regex("""<script[^>]*id=["']anime-data["'][^>]*>([\s\S]*?)</script>""")
            .find(html)?.groupValues?.get(1)
            ?: run {
                Logger.w("Nakanime: no anime-data script on '${anime.slug}'")
                return null
            }

        val root = runCatching { json.parseToJsonElement(script).asObj() }.getOrNull() ?: return null
        val container = root["anime"]?.asObj() ?: root
        val episodesJson = (container["episodesList"] ?: root["episodesList"])?.asArr() ?: return null
        val seasonNumbers = (container["seasons"] ?: root["seasons"])
            ?.asArr()
            ?.mapNotNull { season ->
                val o = season.asObj() ?: return@mapNotNull null
                val id = o["id"]?.asInt() ?: return@mapNotNull null
                id to (o["number"]?.asInt() ?: 1)
            }
            ?.toMap()
            ?: emptyMap()

        val episodes = episodesJson.mapNotNull { element ->
            val o = element.asObj() ?: return@mapNotNull null
            val id = o["id"]?.asInt() ?: return@mapNotNull null
            val number = o["number"]?.asInt() ?: return@mapNotNull null
            val seasonNumber = o["seasonId"]?.asInt()?.let { seasonNumbers[it] } ?: 1
            NakanimeEpisode(
                id = id,
                number = number,
                seasonNumber = seasonNumber,
                hasSources = o["hasSources"]?.asBool() ?: true,
            )
        }
        Logger.i("Nakanime: ${episodes.size} episode(s) listed for '${anime.title}'")
        return episodes
    }

    private fun pickEpisode(episodes: List<NakanimeEpisode>, ref: EpisodeRef): NakanimeEpisode? {
        val number = ref.episode ?: ref.absolute
        val matching = episodes.filter { it.number == number || it.number == ref.absolute }
        val preferred = when {
            ref.season != null -> matching.firstOrNull { it.seasonNumber == ref.season }
            else -> matching.firstOrNull { it.seasonNumber == 1 } ?: matching.firstOrNull()
        } ?: matching.firstOrNull()
        return preferred?.let { candidate ->
            if (candidate.hasSources) candidate else matching.firstOrNull { it.hasSources } ?: candidate
        }
    }

    private suspend fun loadSources(animeId: Int, episodeId: Int): List<NakanimeSource>? {
        val path = "/api/sources/anime"
        val body = """{"anime_id":$animeId,"episode_id":$episodeId,"turnstile_token":""}"""
        val bytes = try {
            http.postJsonBytes("$base$path", body, apiHeaders())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("Nakanime sources failed for anime=$animeId ep=$episodeId: ${e.message}", e)
            return null
        }

        val arr = runCatching { json.parseToJsonElement(NakanimeCodec.decode(bytes, path)).asArr() }.getOrNull()
            ?: return null
        return arr.mapNotNull { element ->
            val o = element.asObj() ?: return@mapNotNull null
            val url = o["url"]?.asStr() ?: return@mapNotNull null
            NakanimeSource(
                url = url,
                host = o["host"]?.asStr().orEmpty(),
                language = o["language"]?.asStr().orEmpty(),
            )
        }
    }

    private suspend fun resolveSource(source: NakanimeSource): Resolved? {
        val embedUrl = source.url
        val host = source.host.lowercase().ifBlank {
            embedUrl.substringAfter("://").substringBefore("/").lowercase()
        }
        return try {
            when {
                "vidmoly" in host || "vidmoly" in embedUrl -> resolveVidmoly(embedUrl)
                "voe" in host || "/e/" in embedUrl -> resolveVoe(embedUrl)
                "sibnet" in host || "sibnet" in embedUrl -> resolveSibnet(embedUrl)
                "ok.ru" in host || "my.mail.ru" in embedUrl || "ok.ru" in embedUrl -> resolveOkRu(embedUrl)
                "sendvid" in host || "sendvid" in embedUrl -> resolveSendvid(embedUrl)
                else -> resolveGeneric(embedUrl)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("Nakanime embed ${source.host} failed: ${e.message}", e)
            null
        }
    }

    private suspend fun resolveVidmoly(embedUrl: String): Resolved? {
        val html = http.getText(embedUrl, embedHeaders(embedUrl))
        val stream = EmbedResolver.extract(embedUrl, html) ?: return null
        return Resolved(stream, originOf(embedUrl) + "/", stream.contains(".m3u8"))
    }

    private suspend fun resolveVoe(embedUrl: String): Resolved? {
        val html = http.getText(embedUrl, embedHeaders(embedUrl))
        val payload = Regex("""<script[^>]*type=["']application/json["'][^>]*>([\s\S]*?)</script>""")
            .find(html)?.groupValues?.get(1)
        val decoded = payload
            ?.let { runCatching { json.parseToJsonElement(it.trim()).asArr()?.firstOrNull()?.asStr() }.getOrNull() }
            ?.let(::voeDecode)

        val stream = decoded
            ?.let { runCatching { json.parseToJsonElement(it).asObj() }.getOrNull() }
            ?.let { it["source"]?.asStr() ?: it["direct_access_url"]?.asStr() }
            ?: Regex("""https?://[^\s"']+\.m3u8[^\s"']*""").find(html)?.value
            ?: return null
        return Resolved(stream, originOf(embedUrl) + "/", stream.contains(".m3u8"))
    }

    private suspend fun resolveSibnet(embedUrl: String): Resolved? {
        val html = http.getText(embedUrl, embedHeaders(embedUrl))
        val src = Regex("""player\.src\(\[\{[^}]*?src:\s*"([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
            .find(html)?.groupValues?.get(1) ?: return null
        val url = when {
            src.startsWith("http") -> src
            src.startsWith("//") -> "https:$src"
            else -> "https://video.sibnet.ru$src"
        }
        return Resolved(url, "https://video.sibnet.ru/", url.contains(".m3u8"))
    }

    private suspend fun resolveOkRu(embedUrl: String): Resolved? {
        val id = Regex("""(?:embed/|video/|_myvideo/)(\d+)""").find(embedUrl)?.groupValues?.get(1) ?: return null
        val text = http.getText("https://my.mail.ru/+/video/meta/$id", embedHeaders("https://my.mail.ru/"))
        val stream = findStreamUrl(text) ?: return null
        return Resolved(stream, "https://my.mail.ru/", stream.contains(".m3u8"))
    }

    private suspend fun resolveSendvid(embedUrl: String): Resolved? {
        val html = http.getText(embedUrl, embedHeaders(embedUrl))
        val src = Regex("""var\s+video_source\s*=\s*"([^"]+)"""").find(html)?.groupValues?.get(1) ?: return null
        val url = normalizeUrl(src)
        return Resolved(url, originOf(embedUrl) + "/", url.contains(".m3u8"))
    }

    /** Vidzy / LuluStream / Uqload / Movearnpre / AnsEmbed and friends: unpacked JS with an m3u8. */
    private suspend fun resolveGeneric(embedUrl: String): Resolved? {
        val html = http.getText(embedUrl, embedHeaders(embedUrl))
        var content = html
        if (content.contains("eval(function")) {
            val script = Regex("""<script[^>]*>([\s\S]*?)</script>""")
                .findAll(content)
                .map { it.groupValues[1] }
                .firstOrNull { it.contains("eval(function") }
            if (script != null) {
                val packed = "eval(function(" + KwikPacker.substringAfterLast(script, "eval(function(")
                KwikPacker.unpack(packed)?.let { content = it }
            }
        }
        val stream = Regex("""https?://[^\s"'<>()]+\.m3u8[^\s"'<>()]*""").find(content)?.value
            ?: Regex("""https?://[^\s"'<>()]+\.mp4[^\s"'<>()]*""").find(content)?.value
            ?: return null
        val url = stream.replace("\\/", "/")
        return Resolved(url, originOf(embedUrl) + "/", url.contains(".m3u8"))
    }

    private fun findStreamUrl(text: String): String? {
        val unescaped = text.replace("\\/", "/").replace("\\u0026", "&")
        Regex("""(?:https?:)?//[^\s"'\\]+\.m3u8[^\s"'\\]*""").find(unescaped)?.value?.let { return normalizeUrl(it) }
        Regex("""(?:https?:)?//[^\s"'\\]+\.mp4[^\s"'\\]*""").find(unescaped)?.value?.let { return normalizeUrl(it) }
        return null
    }

    private fun normalizeUrl(url: String): String =
        if (url.startsWith("//")) "https:$url" else url

    private fun originOf(url: String): String =
        Regex("""https?://[^/]+""").find(url)?.value ?: base

    private fun dubLabel(language: String): String? {
        val upper = language.uppercase()
        return when {
            upper.contains("VOST") -> null
            upper.isBlank() -> null
            else -> upper
        }
    }

    private fun subtitlesFor(language: String): List<SubtitleTrack> =
        if (dubLabel(language) == null) listOf(SubtitleTrack("fr", SubtitleType.Hard)) else emptyList()

    private fun rot13(value: String): String = buildString(value.length) {
        for (c in value) {
            append(
                when (c) {
                    in 'a'..'z' -> ((c - 'a' + 13) % 26 + 'a'.code).toChar()
                    in 'A'..'Z' -> ((c - 'A' + 13) % 26 + 'A'.code).toChar()
                    else -> c
                }
            )
        }
    }

    private fun voeDecode(payload: String): String? {
        var value = rot13(payload)
        listOf("@\$", "^^", "~@", "%?", "*~", "!!", "#&").forEach { value = value.replace(it, "") }
        val first = base64Decode(value) ?: return null
        val shifted = first.map { (it.code - 3).toChar() }.joinToString("")
        return base64Decode(shifted.reversed())
    }

    private fun base64Decode(value: String): String? {
        val cleaned = value.trim().replace("\n", "").replace("\r", "")
        val padded = cleaned + "=".repeat((4 - cleaned.length % 4) % 4)
        return runCatching { Base64.decode(padded).decodeToString() }
            .recoverCatching { Base64.UrlSafe.decode(padded).decodeToString() }
            .getOrNull()
    }

    private fun apiHeaders() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
        "Accept" to "application/json",
    )

    private fun pageHeaders(): Map<String, String> = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language" to "fr-FR,fr;q=0.9,en;q=0.8",
    )

    private fun embedHeaders(embedUrl: String) = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
    )

    private fun JsonElement?.asObj(): JsonObject? = this as? JsonObject

    private fun JsonElement?.asArr(): JsonArray? = this as? JsonArray

    private fun JsonElement?.asStr(): String? = (this as? JsonPrimitive)?.contentOrNull

    private fun JsonElement?.asInt(): Int? = (this as? JsonPrimitive)?.intOrNull

    private fun JsonElement?.asBool(): Boolean? = (this as? JsonPrimitive)?.booleanOrNull
}

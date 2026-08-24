package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

class AniNekoProvider(http: ProviderHttpClient) : BaseProvider(http) {

    override val providerId = "anineko"
    override val providerName = "AniNeko"

    private val base = "https://anineko.to"
    private val vibeHosts = listOf("bibiemb", "vivibebe")
    private val packerHosts = listOf("otakuvid", "otakuhg")

    private data class Server(val embedUrl: String, val dub: Boolean, val subtitleUrl: String?)

    private data class Resolved(val url: String, val referer: String)

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> = coroutineScope {
        Logger.i("AniNeko: starting extraction for '${query.animeTitle}' episode ${query.episode.absolute}")
        val slug = resolveSlug(query)
        if (slug == null) {
            Logger.w("AniNeko: could not resolve slug for '${query.animeTitle}'")
            return@coroutineScope emptyList()
        }
        Logger.i("AniNeko: resolved slug '$slug'")

        val episodeHtml = loadEpisodePage(slug, query.episode.absolute)
        if (episodeHtml == null) {
            Logger.w("AniNeko: no episode page for '$slug' ep ${query.episode.absolute}")
            return@coroutineScope emptyList()
        }

        val servers = extractServers(episodeHtml)
        Logger.d("AniNeko: episode page has ${servers.size} server(s)")
        if (servers.isEmpty()) {
            Logger.w("AniNeko: no server buttons found on episode page")
            return@coroutineScope emptyList()
        }

        val streams = servers.map { server ->
            async {
                val resolved = resolveServer(server)
                if (resolved == null) {
                    Logger.w("AniNeko: could not resolve embed ${server.embedUrl}")
                    return@async null
                }
                Logger.i("AniNeko: resolved stream ${resolved.url} (dub=${server.dub})")
                StreamSource(
                    providerId = providerId,
                    url = resolved.url,
                    quality = null,
                    isM3U8 = true,
                    headers = mapOf("Referer" to resolved.referer),
                    dub = if (server.dub) "en" else null,
                    subtitles = subtitlesFor(server),
                    matchScore = 1f,
                    // These are single-audio HLS streams served as ".txt" (not
                    // ".m3u8"), so the player's audio-track detection falls back to
                    // raw VLC track descriptions and can surface spurious duplicates.
                    supportsAudioTrackSelection = false,
                )
            }
        }.awaitAll().filterNotNull()

        Logger.i("AniNeko: extracted ${streams.size} stream(s) for '${query.animeTitle}' ep ${query.episode.absolute}")
        streams
    }

    private suspend fun resolveSlug(query: EpisodeSearchQuery): String? {
        val searchUrl = "$base/browser?keyword=${encode(query.animeTitle)}"
        Logger.d("AniNeko: searching '${query.animeTitle}' -> $searchUrl")
        val html = try {
            http.getText(searchUrl, headers())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.w("AniNeko search failed: ${e.message}", e)
            return null
        }

        val resultRegex = Regex(
            """<article class="nv-anime-card[^"]*">[\s\S]*?href="/watch/([a-z0-9-]+)"[\s\S]*?<h3 class="nv-anime-title"><a href="/watch/[^"]+">([^<]*)</a></h3>"""
        )
        var best: Pair<String, Float>? = null
        for (match in resultRegex.findAll(html)) {
            val slug = match.groupValues[1]
            val title = decodeHtmlEntities(match.groupValues[2]).trim()
            val score = bestTitleScore(query, title)
            if (best == null || score > best.second) best = slug to score
        }
        Logger.i("AniNeko: best slug='${best?.first}' score=${best?.second}")
        return best?.first
    }

    private suspend fun loadEpisodePage(slug: String, episode: Int): String? {
        val direct = "$base/watch/$slug/ep-$episode"
        Logger.d("AniNeko: fetching episode page $direct")
        val directHtml = try {
            http.getText(direct, headers())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.w("AniNeko episode page failed for $direct: ${e.message}")
            null
        }
        if (directHtml != null && directHtml.contains("data-video")) {
            Logger.d("AniNeko: episode page fetched (length=${directHtml.length})")
            return directHtml
        }

        val animeHtml = try {
            http.getText("$base/watch/$slug", headers())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.w("AniNeko anime page failed for $slug: ${e.message}")
            return null
        }
        val target = Regex("""href="(/watch/$slug/ep-(\d+))"""")
            .findAll(animeHtml)
            .mapNotNull { m ->
                val n = m.groupValues[2].toIntOrNull() ?: return@mapNotNull null
                m.groupValues[1] to n
            }
            .minByOrNull { abs(it.second - episode) }
            ?.first
            ?: run {
                Logger.w("AniNeko: no episode link found on anime page for '$slug' ep $episode")
                return null
            }
        Logger.d("AniNeko: scanning anime page, using episode link $target")
        return try {
            http.getText(if (target.startsWith("http")) target else base + target, headers())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.w("AniNeko episode page failed for $target: ${e.message}")
            null
        }
    }

    private fun extractServers(html: String): List<Server> {
        val servers = mutableListOf<Server>()
        val panelOpener = Regex("""<div class="nv-server-grid[^"]*" data-id="(sub|dub)">""")
        val panels = panelOpener.findAll(html).toList()
        for ((index, match) in panels.withIndex()) {
            val isDub = match.groupValues[1] == "dub"
            val start = match.range.last + 1
            val end = if (index + 1 < panels.size) panels[index + 1].range.first else html.length
            val panel = html.substring(start, end)
            for (button in Regex("""data-video="([^"]+)"""").findAll(panel)) {
                val embedUrl = button.groupValues[1]
                servers.add(Server(embedUrl, isDub, subtitleFromEmbed(embedUrl)))
            }
        }
        return servers
    }

    private suspend fun resolveServer(server: Server): Resolved? {
        val embedHost = server.embedUrl.substringAfter("://").substringBefore("/").lowercase()
        val embedHtml = try {
            http.getText(server.embedUrl, headers())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.w("AniNeko embed fetch failed for ${server.embedUrl}: ${e.message}")
            return null
        }
        val stream = when {
            vibeHosts.any { embedHost.contains(it) } -> extractVibeStream(embedHtml)
            packerHosts.any { embedHost.contains(it) } -> extractPackerStream(embedHtml)
            else -> {
                Logger.d("AniNeko: skipping unsupported embed host $embedHost")
                null
            }
        }
        if (stream == null) {
            Logger.w("AniNeko: no m3u8 found in embed ${server.embedUrl}")
            return null
        }
        val referer = Regex("""https?://[^/]+""").find(server.embedUrl)?.value?.plus("/") ?: "$base/"
        return Resolved(stream, referer)
    }

    private fun extractVibeStream(html: String): String? =
        Regex("""const src = "([^"]+\.m3u8[^"]*)"""")
            .find(html)?.groupValues?.get(1)?.replace("\\/", "/")
            ?: Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(html)?.value?.replace("\\/", "/")

    /**
     * otakuvid / otakuhg embeds hide their HLS links inside a JS p.a.c.k.e.r
     * blob (links.hls2/3/4). The packs unpack with the classic unpacker already
     * ported for AnimePahe (KwikPacker). The dramiyos-cdn hls2 variant is bound
     * to a per-session token and 403s plain requests, so it is skipped.
     */
    private fun extractPackerStream(html: String): String? {
        val packed = Regex("""<script[^>]*>([\s\S]*?)</script>""")
            .findAll(html)
            .map { it.groupValues[1] }
            .firstOrNull { it.contains("eval(function") }
        if (packed != null) {
            val scriptPart = KwikPacker.substringAfterLast(packed, "eval(function(")
            val unpacked = KwikPacker.unpack("eval(function(" + scriptPart)
            if (unpacked != null) {
                val links = Regex(""""hls(\d)":"([^"]+)"""")
                    .findAll(unpacked)
                    .mapNotNull { match ->
                        val url = match.groupValues[2].replace("\\/", "/")
                        if (url.contains("dramiyos-cdn")) null else (match.groupValues[1].toIntOrNull() ?: 0) to url
                    }
                    .toList()
                val best = links.maxByOrNull { it.first }?.second
                if (best != null) return best
            }
        }
        return Regex("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").find(html)?.value?.replace("\\/", "/")
    }

    private fun subtitlesFor(server: Server): List<SubtitleTrack> {
        val subtitleUrl = server.subtitleUrl
        return if (server.dub) {
            emptyList()
        } else if (subtitleUrl != null) {
            listOf(SubtitleTrack("en", SubtitleType.Soft, subtitleUrl))
        } else {
            listOf(SubtitleTrack("en", SubtitleType.Hard))
        }
    }

    private fun subtitleFromEmbed(embedUrl: String): String? {
        val query = embedUrl.substringAfter('?', "")
        for (key in listOf("sub=", "caption_1=", "c1_file=")) {
            val value = query.substringAfter(key, "").substringBefore('&')
            if (value.startsWith("http")) return value
        }
        return null
    }

    private fun decodeHtmlEntities(value: String): String = value
        .replace("&#039;", "'")
        .replace("&#x27;", "'")
        .replace("&apos;", "'")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    private fun headers() = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
    )
}
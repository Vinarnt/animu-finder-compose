package fr.vinarnt.animu.finder.compose.repository.extractor

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HiAnimeExtractor(http: ExtractorHttpClient) : BaseExtractor(http) {

    override val providerId = "hianime"
    override val providerName = "HiAnime"

    private val mirrors = listOf("https://hianime.to", "https://aniwatch.to", "https://hianime.mobi")

    @Volatile
    private var activeBase: String? = null

    private data class EpisodeEntry(val number: Int, val id: String, val isDub: Boolean)

    private data class ServerEntry(val id: String, val isDub: Boolean)

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> {
        val animeId = resolveAnimeId(query) ?: return emptyList()
        val base = currentBase()
        val entries = loadEpisodes(base, animeId)
        val target = query.episode.absolute
        val entry = entries.firstOrNull { it.number == target }
            ?: entries.filter { !it.isDub }.minByOrNull { kotlin.math.abs(it.number - target) }
            ?: return emptyList()

        val servers = loadServers(base, entry.id, entry.isDub)
        return servers.flatMap { server ->
            loadSources(base, entry.id, server)
        }
    }

    private suspend fun resolveAnimeId(query: EpisodeSearchQuery): String? {
        val bases = if (activeBase != null) listOf(activeBase!!) else mirrors
        var lastError: Exception? = null
        for (base in bases) {
            try {
                val text = http.getText(
                    "$base/ajax/search/suggest?keyword=${encode(query.animeTitle)}",
                    baseHeaders(base),
                )
                val root = json.parseToJsonElement(text).jsonObject
                val suggestions = root["suggestions"]?.jsonArray ?: emptyList()

                var best: Pair<String, Float>? = null
                for (item in suggestions) {
                    val obj = item.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: continue
                    val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
                    val score = bestTitleScore(query, name)
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
        throw lastError ?: Exception("HiAnime: all mirrors unreachable")
    }

    private fun currentBase(): String = activeBase ?: mirrors.first()

    private suspend fun loadEpisodes(base: String, animeId: String): List<EpisodeEntry> {
        val text = http.getText("$base/ajax/v2/episode/list/$animeId", baseHeaders(base))
        val root = json.parseToJsonElement(text).jsonObject
        val html = root["html"]?.jsonPrimitive?.contentOrNull ?: return emptyList()

        val entries = mutableListOf<EpisodeEntry>()
        val regex = Regex("""data-number="(\d+)"[^>]*data-id="(\d+)"""")
        for (match in regex.findAll(html)) {
            val number = match.groupValues[1].toIntOrNull() ?: continue
            entries.add(EpisodeEntry(number, match.groupValues[2], isDub = false))
        }
        val seen = mutableMapOf<Int, Int>()
        for (e in entries) {
            seen[e.number] = (seen[e.number] ?: 0) + 1
        }
        val firstIds = entries.groupBy { it.number }.mapValues { it.value.first().id }
        return entries.map { e ->
            e.copy(isDub = (seen[e.number] ?: 0) > 1 && e.id != firstIds[e.number])
        }
    }

    private suspend fun loadServers(base: String, episodeId: String, preferDub: Boolean): List<ServerEntry> {
        val text = http.getText("$base/ajax/v2/episode/servers?episodeId=$episodeId", baseHeaders(base))
        val root = json.parseToJsonElement(text).jsonObject
        val html = root["html"]?.jsonPrimitive?.contentOrNull ?: return emptyList()

        val servers = mutableListOf<ServerEntry>()
        val regex = Regex("""data-server-id="(\d+)"[^>]*data-type="(sub|dub)"""")
        for (match in regex.findAll(html)) {
            servers.add(ServerEntry(match.groupValues[1], match.groupValues[2] == "dub"))
        }
        return if (preferDub) {
            servers.filter { it.isDub }.ifEmpty { servers }
        } else {
            servers.filter { !it.isDub }.ifEmpty { servers }
        }
    }

    private suspend fun loadSources(base: String, episodeId: String, server: ServerEntry): List<StreamSource> {
        return try {
            val text = http.getText("$base/ajax/v2/episode/sources?id=${server.id}", baseHeaders(base))
            val root = json.parseToJsonElement(text).jsonObject
            val link = root["link"]?.jsonPrimitive?.contentOrNull ?: return emptyList()
            val trackList = root["tracks"]?.jsonArray ?: emptyList()
            val subtitles = trackList.mapNotNull { track ->
                val obj = track.jsonObject
                val label = obj["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val file = obj["file"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val kind = obj["kind"]?.jsonPrimitive?.contentOrNull
                if (kind == "captions") SubtitleTrack(label, SubtitleType.Soft, file, null) else null
            }

            listOf(
                StreamSource(
                    providerId = providerId,
                    url = link,
                    quality = null,
                    isM3U8 = link.contains(".m3u8"),
                    headers = mapOf("Referer" to "$base/"),
                    dub = if (server.isDub) "en" else null,
                    subtitles = subtitles,
                    matchScore = 1f,
                )
            )
        } catch (e: Exception) {
            Logger.w("HiAnime source extraction failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun baseHeaders(base: String) = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to "$base/",
        "X-Requested-With" to "XMLHttpRequest",
    )
}

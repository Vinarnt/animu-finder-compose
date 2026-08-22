package fr.vinarnt.animu.finder.compose.repository

import fr.vinarnt.animu.finder.compose.model.SubtitleCue
import fr.vinarnt.animu.finder.compose.repository.provider.ProviderHttpClient
import kotlinx.coroutines.CancellationException

/**
 * Loads external WebVTT subtitles for the player. The player library's JVM
 * backend ignores subtitle tracks, so soft subs are fetched and parsed here and
 * rendered as an overlay by the player UI.
 */
class SubtitleRepository(private val http: ProviderHttpClient) {

    suspend fun load(url: String): List<SubtitleCue> =
        try {
            parseWebVtt(http.getText(url))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
}

fun parseWebVtt(content: String): List<SubtitleCue> {
    val cues = mutableListOf<SubtitleCue>()
    val lines = content.lines()
    var index = 0
    while (index < lines.size) {
        val line = lines[index].trim()
        if (!line.contains("-->")) {
            index++
            continue
        }
        val timings = line.split("-->")
        val start = parseVttTimestamp(timings[0].trim())
        val end = parseVttTimestamp(timings[1].trim().substringBefore(' '))
        index++
        val text = StringBuilder()
        while (index < lines.size) {
            val candidate = lines[index]
            if (candidate.trim().isEmpty() || candidate.contains("-->")) break
            if (text.isNotEmpty()) text.append('\n')
            text.append(stripCueTags(candidate))
            index++
        }
        if (start != null && end != null && text.isNotBlank()) {
            cues.add(SubtitleCue(start, end, text.toString()))
        }
    }
    return cues
}

private fun parseVttTimestamp(value: String): Long? {
    val withHours = Regex("(\\d+):(\\d+):(\\d+)[.,](\\d+)").find(value)
    if (withHours != null) {
        val g = withHours.groupValues
        return g[1].toLong() * 3_600_000 +
            g[2].toLong() * 60_000 +
            g[3].toLong() * 1_000 +
            g[4].padEnd(3, '0').take(3).toLong()
    }
    val withMinutes = Regex("(\\d+):(\\d+)[.,](\\d+)").find(value) ?: return null
    val g = withMinutes.groupValues
    return g[1].toLong() * 60_000 +
        g[2].toLong() * 1_000 +
        g[3].padEnd(3, '0').take(3).toLong()
}

private fun stripCueTags(value: String): String = value
    .replace(Regex("<[^>]*>"), "")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&#39;", "'")
    .replace("&quot;", "\"")
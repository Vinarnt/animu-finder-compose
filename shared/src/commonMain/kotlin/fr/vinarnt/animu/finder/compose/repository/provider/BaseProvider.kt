package fr.vinarnt.animu.finder.compose.repository.provider

import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json

abstract class BaseProvider(
    protected val http: ProviderHttpClient,
) : StreamingProvider {

    protected val json = Json { ignoreUnknownKeys = true }

    protected fun encode(value: String): String = value.encodeURLParameter()

    protected fun normalize(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    protected fun similarity(a: String, b: String): Float {
        val na = normalize(a)
        val nb = normalize(b)
        if (na.isEmpty() || nb.isEmpty()) return 0f
        if (na == nb) return 1f
        val distance = levenshtein(na, nb)
        val maxLength = maxOf(na.length, nb.length)
        return 1f - (distance.toFloat() / maxLength.toFloat())
    }

    protected fun matchScore(title: String, candidate: String, episodeExact: Boolean): Float =
        similarity(title, candidate) * if (episodeExact) 1f else 0.5f

    protected fun bestTitleScore(query: EpisodeSearchQuery, candidate: String): Float {
        val titles = buildList {
            add(query.animeTitle)
            addAll(query.altTitles)
        }.filter { it.isNotBlank() }
        return titles.maxOfOrNull { similarity(it, candidate) } ?: 0f
    }

    protected fun matchesEpisodeNumber(label: String?, episode: EpisodeRef): Boolean {
        if (label == null) return false
        val number = Regex("\\d+").find(label)?.value?.toIntOrNull() ?: return false
        return number == episode.absolute || number == episode.episode
    }

    private fun levenshtein(a: String, b: String): Int {
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            for (j in 0..b.length) prev[j] = curr[j]
        }
        return prev[b.length]
    }
}

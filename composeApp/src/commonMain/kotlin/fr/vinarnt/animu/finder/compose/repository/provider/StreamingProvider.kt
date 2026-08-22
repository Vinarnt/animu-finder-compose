package fr.vinarnt.animu.finder.compose.repository.provider

import fr.vinarnt.animu.finder.compose.model.StreamSource

interface StreamingProvider {
    val providerId: String
    val providerName: String
    suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource>
}

data class EpisodeRef(
    val absolute: Int,
    val season: Int? = null,
    val episode: Int? = null,
) {
    fun formats(): List<String> = buildList {
        add("Episode $absolute")
        add(absolute.toString())
        if (season != null && episode != null) {
            add("S${season}E${episode}")
            add("${season}x${episode}")
            add("Season $season Episode $episode")
        }
    }
}

data class EpisodeSearchQuery(
    val animeTitle: String,
    val altTitles: List<String>,
    val episode: EpisodeRef,
    val totalEpisodes: Int? = null,
)

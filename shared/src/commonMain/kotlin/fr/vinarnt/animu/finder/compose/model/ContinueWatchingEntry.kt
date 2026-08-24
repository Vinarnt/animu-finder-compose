package fr.vinarnt.animu.finder.compose.model

import kotlinx.serialization.Serializable

@Serializable
data class ContinueWatchingEntry(
    val animeId: Int,
    val episodeNumber: Int,
)

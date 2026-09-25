package fr.vinarnt.animu.finder.compose.model

import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import kotlinx.serialization.Serializable

@Serializable
data class ContinueWatchingAnime(
    val malId: Int? = null,
    val title: String? = null,
    val altTitles: List<String> = emptyList(),
    val posterUrl: String? = null,
    val episodes: Int? = null,
)

fun GetAnimeById200ResponseData.toContinueWatchingAnime(): ContinueWatchingAnime = ContinueWatchingAnime(
    malId = malId,
    title = titles?.firstOrNull { it.type in listOf("English", "Default") }?.title
        ?: titles?.firstOrNull()?.title,
    altTitles = titles?.mapNotNull { it.title }.orEmpty(),
    posterUrl = images?.webp?.largeImageUrl ?: images?.jpg?.largeImageUrl,
    episodes = episodes,
)

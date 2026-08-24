package fr.vinarnt.animu.finder.compose.model

import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.resolveAnimeTitle
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import kotlinx.serialization.Serializable

@Serializable
data class ContinueWatchingAnime(
    val malId: Int? = null,
    val title: String = "Unknown",
    val altTitles: List<String> = emptyList(),
    val posterUrl: String? = null,
    val episodes: Int? = null,
)

fun GetAnimeById200ResponseData.toContinueWatchingAnime(): ContinueWatchingAnime = ContinueWatchingAnime(
    malId = malId,
    title = resolveAnimeTitle(this),
    altTitles = titles?.mapNotNull { it.title }.orEmpty(),
    posterUrl = images?.webp?.largeImageUrl ?: images?.jpg?.largeImageUrl,
    episodes = episodes,
)

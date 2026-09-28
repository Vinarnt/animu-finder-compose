package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.Shelf
import fr.vinarnt.animu.finder.compose.util.platformImageUrl
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner

/** Horizontal shelf of the title's episodes, highlighting the current one. */
@Composable
internal fun EpisodeShelf(
    episodes: List<GetAnimeByIdEpisodes200ResponseDataInner>,
    episodeNumber: Int,
    onSelectEpisode: (Int) -> Unit,
    fallbackImageUrl: String? = null,
) {
    val s = strings.episodeDetail

    Shelf(
        title = "${strings.animeDetail.episodesLabel} · ${episodes.size}",
        seeAll = false,
    ) {
        items(
            items = episodes,
            key = { ep -> "shelf:${ep.malId ?: episodes.indexOf(ep)}" },
        ) { ep ->
            val number = ep.malId
            EpisodeShelfCard(
                number = number ?: 0,
                numberLabel = s.episodeShort.replace("{number}", (number ?: 0).toString()),
                title = ep.title ?: s.episodeTitleFallback(number ?: 0),
                subtitle = ep.aired?.take(10),
                nowPlayingLabel = s.nowPlaying,
                imageUrl = ep.images?.jpg?.imageUrl?.let { platformImageUrl(it) } ?: fallbackImageUrl,
                isActive = number == episodeNumber,
                onClick = { number?.let(onSelectEpisode) },
            )
        }
    }
}

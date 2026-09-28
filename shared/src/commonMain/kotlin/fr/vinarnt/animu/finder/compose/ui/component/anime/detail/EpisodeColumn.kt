package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyColumn
import io.github.ahmad_hamwi.compose.pagination.PaginationState

/** Paginated episode list for the list-style designs (minimal / poster). */
@Composable
internal fun EpisodeColumn(
    paginationState: PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner>,
    episodes: List<GetAnimeByIdEpisodes200ResponseDataInner>,
    episodeDisplay: EpisodeDisplay,
    gutter: Dp,
    activeEpisodeNumber: Int?,
    onEpisodeClick: (GetAnimeByIdEpisodes200ResponseDataInner) -> Unit,
    lazyListState: LazyListState,
    fallbackImageUrl: String? = null,
) {
    PaginatedLazyColumn(
        paginationState = paginationState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = PaddingValues(
            start = gutter,
            end = gutter,
            top = Spacing.xs,
            bottom = Spacing.sm
        ),
        state = lazyListState,
        firstPageProgressIndicator = { EpisodePageProgress(Size.CircleProgressIndicator.md) },
        newPageProgressIndicator = { EpisodePageProgress(Size.CircleProgressIndicator.sm) },
        newPageErrorIndicator = { EpisodePageError { paginationState.retryLastFailedRequest() } },
    ) {
        items(
            count = episodes.size,
            key = { index -> "episode:${episodes[index].malId ?: index}" }
        ) { index ->
            val episode = episodes[index]
            val isActive = episode.malId == activeEpisodeNumber
            when (episodeDisplay) {
                EpisodeDisplay.MINIMAL -> EpisodeItemDesignMinimal(
                    episode = episode,
                    onClick = { onEpisodeClick(episode) },
                    isActive = isActive,
                )
                EpisodeDisplay.POSTER -> EpisodeItemDesignPoster(
                    episode = episode,
                    onClick = { onEpisodeClick(episode) },
                    isActive = isActive,
                    fallbackImageUrl = fallbackImageUrl,
                )
                EpisodeDisplay.GRID -> Unit
            }
        }
    }
}

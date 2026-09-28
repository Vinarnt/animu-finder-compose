package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import io.github.ahmad_hamwi.compose.pagination.PaginationState

/** Paginated episode grid (two fixed columns when compact, adaptive otherwise). */
@Composable
internal fun EpisodeGrid(
    paginationState: PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner>,
    episodes: List<GetAnimeByIdEpisodes200ResponseDataInner>,
    compact: Boolean,
    gutter: Dp,
    activeEpisodeNumber: Int?,
    onEpisodeClick: (GetAnimeByIdEpisodes200ResponseDataInner) -> Unit,
    fallbackImageUrl: String? = null,
) {
    PaginatedLazyVerticalGrid(
        paginationState = paginationState,
        columns = if (compact) {
            GridCells.Fixed(2)
        } else {
            GridCells.Adaptive(minSize = 220.dp)
        },
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = gutter,
            end = gutter,
            top = Spacing.xs,
            bottom = Spacing.sm
        ),
        firstPageProgressIndicator = { EpisodePageProgress(Size.CircleProgressIndicator.md) },
        newPageProgressIndicator = { EpisodePageProgress(Size.CircleProgressIndicator.sm) },
        newPageErrorIndicator = { EpisodePageError { paginationState.retryLastFailedRequest() } },
    ) {
        items(
            count = episodes.size,
            key = { index -> "episode:${episodes[index].malId ?: index}" }
        ) { index ->
            val episode = episodes[index]
            EpisodeItemDesignGrid(
                episode = episode,
                onClick = { onEpisodeClick(episode) },
                isActive = episode.malId == activeEpisodeNumber,
                fallbackImageUrl = fallbackImageUrl,
            )
        }
    }
}

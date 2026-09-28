package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.ui.theme.Breakpoints
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData

/**
 * Episode detail body: the episode header card, the player + sources section and,
 * when available, a shelf of the title's episodes.
 */
@Composable
fun EpisodeDetailLayout(
    episode: GetAnimeByIdEpisodesByEpisodeId200ResponseData?,
    episodeNumber: Int,
    streams: List<StreamSource>,
    selectedStream: StreamSource?,
    loadingStreams: Boolean,
    nextEpisode: GetAnimeByIdEpisodesByEpisodeId200ResponseData? = null,
    episodes: List<GetAnimeByIdEpisodes200ResponseDataInner> = emptyList(),
    fallbackImageUrl: String? = null,
    onSelectStream: (StreamSource) -> Unit,
    onSelectEpisode: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    playerContent: (@Composable () -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val compact = maxWidth < Breakpoints.compactMaxWidth
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        val gutter = sidePadding + if (compact) 16.dp else 24.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = gutter,
                end = gutter,
                top = Spacing.md,
                bottom = Spacing.md,
            ),
        ) {
            item(key = "header") {
                EpisodeHeader(episode, episodeNumber)
            }

            item(key = "player+sources") {
                EpisodePlayerSection(
                    selectedStream = selectedStream,
                    playerContent = playerContent,
                    isFullscreen = isFullscreen,
                    nextEpisode = nextEpisode,
                    episodeNumber = episodeNumber,
                    streams = streams,
                    loadingStreams = loadingStreams,
                    onSelectStream = onSelectStream,
                    onPlayNext = onPlayNext,
                    fallbackImageUrl = fallbackImageUrl,
                )
            }

            if (episodes.isNotEmpty()) {
                item(key = "episode-shelf") {
                    EpisodeShelf(
                        episodes = episodes,
                        episodeNumber = episodeNumber,
                        onSelectEpisode = onSelectEpisode,
                        fallbackImageUrl = fallbackImageUrl,
                    )
                }
            }
        }
    }
}

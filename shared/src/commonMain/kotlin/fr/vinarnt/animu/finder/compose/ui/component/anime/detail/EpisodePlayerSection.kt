package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData

/**
 * The player plus its side content.
 */
@Composable
internal fun EpisodePlayerSection(
    selectedStream: StreamSource?,
    playerContent: (@Composable () -> Unit)?,
    isFullscreen: Boolean,
    nextEpisode: GetAnimeByIdEpisodesByEpisodeId200ResponseData?,
    episodeNumber: Int,
    streams: List<StreamSource>,
    loadingStreams: Boolean,
    onSelectStream: (StreamSource) -> Unit,
    onPlayNext: (() -> Unit)?,
    fallbackImageUrl: String? = null,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val playerSection: @Composable () -> Unit = {
            when {
                // The player is composed in the floating fullscreen overlay instead.
                isFullscreen -> Unit
                selectedStream != null && playerContent != null -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CornerRadius.md),
                        color = Color.Black,
                        tonalElevation = Elevation.sm,
                    ) {
                        playerContent()
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = strings.episodeDetail.noSource,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        if (maxWidth >= 800.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    playerSection()
                }
                Column(
                    modifier = Modifier.width(300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UpNextCard(
                        nextEpisode = nextEpisode,
                        nextEpisodeNumber = episodeNumber + 1,
                        onClick = onPlayNext,
                        fallbackImageUrl = fallbackImageUrl,
                    )
                    StreamSourceList(
                        streams = streams,
                        selectedStream = selectedStream,
                        loading = loadingStreams,
                        onSelect = onSelectStream,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                playerSection()
                UpNextCard(
                    nextEpisode = nextEpisode,
                    nextEpisodeNumber = episodeNumber + 1,
                    onClick = onPlayNext,
                    fallbackImageUrl = fallbackImageUrl,
                )
                StreamSourceList(
                    streams = streams,
                    selectedStream = selectedStream,
                    loading = loadingStreams,
                    onSelect = onSelectStream,
                )
            }
        }
    }
}

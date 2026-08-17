package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.repository.extractor.ProviderError
import fr.vinarnt.animu.finder.compose.ui.component.player.StreamingVideoPlayer
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.AnimeEpisode

@Composable
fun EpisodeDetailLayout(
    episode: AnimeEpisode?,
    episodeNumber: Int,
    streams: List<StreamSource>,
    streamErrors: List<ProviderError>,
    selectedStream: StreamSource?,
    loadingStreams: Boolean,
    onSelectStream: (StreamSource) -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    playerContent: (@Composable () -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item(key = "header") {
            EpisodeHeader(episode, episodeNumber)
        }

        item(key = "player") {
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

        item(key = "sources") {
            StreamSourceList(
                streams = streams,
                errors = streamErrors,
                selectedStream = selectedStream,
                loading = loadingStreams,
                onSelect = onSelectStream,
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
        }
    }
}

@Composable
private fun EpisodeHeader(episode: AnimeEpisode?, episodeNumber: Int) {
    val s = strings.episodeDetail

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm),
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Elevation.sm,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = episode?.title ?: "Episode $episodeNumber",
                style = MaterialTheme.typography.titleLarge,
            )

            val alternativeTitles = buildList {
                episode?.titleJapanese?.let { add(it) }
                episode?.titleRomanji?.let { add(it) }
            }
            if (alternativeTitles.isNotEmpty()) {
                Text(
                    text = "${s.alternativeTitles}: ${alternativeTitles.joinToString(" / ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            episode?.aired?.let { aired ->
                Text(
                    text = "${s.airingDate}: ${aired.take(10)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            episode?.synopsis?.let { synopsis ->
                Text(
                    text = "${s.synopsis}: $synopsis",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

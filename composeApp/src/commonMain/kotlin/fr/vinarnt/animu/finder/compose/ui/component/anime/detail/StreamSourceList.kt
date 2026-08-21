package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
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
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import fr.vinarnt.animu.finder.compose.repository.extractor.ProviderError
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun StreamSourceList(
    streams: List<StreamSource>,
    errors: List<ProviderError>,
    selectedStream: StreamSource?,
    loading: Boolean,
    onSelect: (StreamSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings.episodeDetail

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = s.streams,
            style = MaterialTheme.typography.titleMedium,
        )

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.sm))
                }
            }

            streams.isEmpty() && errors.isEmpty() -> {
                Text(
                    text = s.noStreams,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            streams.isEmpty() -> {
                Text(
                    text = s.couldNotLoadStreams,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                errors.forEach { error ->
                    Text(
                        text = "• ${error.providerName}: ${error.message}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> streams.forEach { source ->
                StreamSourceItem(
                    source = source,
                    selected = source == selectedStream,
                    onClick = { onSelect(source) },
                )
            }
        }
    }
}

@Composable
private fun StreamSourceItem(
    source: StreamSource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val s = strings.episodeDetail

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.sm),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        tonalElevation = Elevation.sm,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(2.dp),
                    ),
            )

            Text(
                text = source.providerId,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )

            source.dub?.let { dub ->
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("${s.dub} $dub", style = MaterialTheme.typography.labelSmall)
                }
            }

            val hardSub = source.subtitles.firstOrNull { it.type == SubtitleType.Hard }
            if (source.dub == null && hardSub != null) {
                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                    Text("${s.sub} ${hardSub.language ?: ""}", style = MaterialTheme.typography.labelSmall)
                }
            } else if (source.subtitles.isNotEmpty()) {
                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                    Text(s.sub, style = MaterialTheme.typography.labelSmall)
                }
            }

            source.quality?.let { quality ->
                Text(
                    text = quality,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

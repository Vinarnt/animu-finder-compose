package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner

@Composable
fun EpisodeItemDesignMinimal(
    episode: GetAnimeByIdEpisodes200ResponseDataInner,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val s = strings.episodeDetail

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.sm),
        color = if (isHovered) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (isHovered) Elevation.md else Elevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(Size.Icon.sm * 3)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(Size.Icon.sm * 3),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${episode.malId ?: "?"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = episode.title ?: "Episode ${episode.malId ?: "?"}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    episode.aired?.let { aired ->
                        Text(
                            text = aired.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (episode.filler == true) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(text = s.filler, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (episode.recap == true) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(text = s.recap, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            episode.score?.let { score ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.widthIn(min = 40.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

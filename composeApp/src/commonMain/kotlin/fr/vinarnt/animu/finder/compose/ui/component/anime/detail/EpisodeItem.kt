package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.AnimeEpisodesAllOfData

@Composable
fun EpisodeItem(
    episode: AnimeEpisodesAllOfData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.sm),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = Elevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "#${episode.malId ?: "?"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = episode.title ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (episode.filler == true) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(text = "Filler", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (episode.recap == true) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(text = "Recap", style = MaterialTheme.typography.labelSmall)
                }
            }
            episode.aired?.let { date ->
                Text(
                    // TODO: Use kotlinx-datetime to format
                    text = date.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
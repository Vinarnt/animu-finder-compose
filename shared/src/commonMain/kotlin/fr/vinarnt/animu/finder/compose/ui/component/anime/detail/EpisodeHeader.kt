package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData

/** Episode title card with alternative titles, air date and synopsis. */
@Composable
internal fun EpisodeHeader(episode: GetAnimeByIdEpisodesByEpisodeId200ResponseData?, episodeNumber: Int) {
    val s = strings.episodeDetail

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = Elevation.sm,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = if (episode?.title != null) {
                    "${s.episodeNumber.replace("{number}", episodeNumber.toString())} · ${episode.title}"
                } else {
                    s.episodeNumber.replace("{number}", episodeNumber.toString())
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            val alternativeTitles = buildList {
                episode?.titleJapanese?.let { add(it) }
                episode?.titleRomanji?.let { add(it) }
            }
            if (alternativeTitles.isNotEmpty()) {
                Text(
                    text = "${s.alternativeTitles}: ${alternativeTitles.joinToString(" / ")}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            episode?.aired?.let { aired ->
                Text(
                    text = "${s.airingDate}: ${aired.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            episode?.synopsis?.let { synopsis ->
                Text(
                    text = "${strings.common.synopsis}: $synopsis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

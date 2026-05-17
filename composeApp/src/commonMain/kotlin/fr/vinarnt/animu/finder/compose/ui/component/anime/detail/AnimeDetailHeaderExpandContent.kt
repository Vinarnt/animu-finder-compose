package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun AnimeDetailHeaderExpandContent(synopsis: String) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        synopsis.let { synopsis ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = strings.animeDetail.synopsisLabel,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(CornerRadius.md),
                    tonalElevation = Elevation.md
                ) {
                    Text(
                        modifier = Modifier.padding(Spacing.md),
                        text = sanitizeSynopsis(synopsis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun sanitizeSynopsis(synopsis: String): String {
    val lastNewline = synopsis.lastIndexOf('\n')
    return when {
        synopsis.substring(lastNewline + 1).contentEquals("[Written by MAL Rewrite]") ->
            synopsis
                .substring(0, lastNewline)
                .trimEnd()

        else -> synopsis
    }
}
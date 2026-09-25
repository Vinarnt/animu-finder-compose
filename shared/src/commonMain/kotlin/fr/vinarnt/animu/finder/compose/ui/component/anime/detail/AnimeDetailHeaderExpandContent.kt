package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.ShelfTitleTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.SynopsysTypography
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun AnimeDetailHeaderExpandContent(anime: GetAnimeById200ResponseData) {
    val synopsis = anime.synopsis

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = strings.common.synopsis,
            style = ShelfTitleTypography,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (synopsis != null) {
            Text(
                text = sanitizeSynopsis(synopsis),
                style = SynopsysTypography,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
            )
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
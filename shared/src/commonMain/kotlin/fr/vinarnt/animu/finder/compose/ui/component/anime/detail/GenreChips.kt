package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInnerProducersInner

@Composable
fun GenreChips(genres: List<GetAnime200ResponseDataInnerProducersInner>?, modifier: Modifier = Modifier) {
    val genreNames = strings.animeList.genreNames
    val labels = genres.orEmpty().mapNotNull { malUrl ->
        val genre = AnimeGenre.entries.find { it.id == malUrl.malId }
        genre?.let { genreNames[it] } ?: malUrl.name
    }
    if (labels.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        labels.forEach { label ->
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                )
            }
        }
    }
}

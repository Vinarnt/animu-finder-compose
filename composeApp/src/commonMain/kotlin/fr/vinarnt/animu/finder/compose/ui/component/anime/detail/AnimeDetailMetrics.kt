package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.scoreGold
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun AnimeDetailMetrics(anime: GetAnimeById200ResponseData, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        anime.score?.let {
            AnimeDetailMetric(
                icon = Icons.Default.Star,
                value = it.toString(),
                description = strings.animeDetail.scoreDescription,
                tint = scoreGold
            )
        }
        anime.rank?.let {
            AnimeDetailMetric(
                icon = Icons.Default.BarChart,
                value = it.toString(),
                description = strings.animeDetail.rankDescription
            )
        }
        anime.popularity?.let {
            AnimeDetailMetric(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                value = it.toString(),
                description = strings.animeDetail.popularityDescription
            )
        }
    }
}

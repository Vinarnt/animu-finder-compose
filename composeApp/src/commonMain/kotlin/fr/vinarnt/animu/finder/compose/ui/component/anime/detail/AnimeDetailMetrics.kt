package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.jikan4k.models.Anime

@Composable
fun AnimeDetailMetrics(anime: Anime) {
    anime.episodes?.let {
        AnimeDetailMetric(
            icon = Icons.Default.Theaters,
            value = it.toString(),
            description = strings.animeDetail.episodesDescription
        )
    }
    anime.score?.let {
        AnimeDetailMetric(
            icon = Icons.Default.Star,
            value = it.toString(),
            description = strings.animeDetail.scoreDescription
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
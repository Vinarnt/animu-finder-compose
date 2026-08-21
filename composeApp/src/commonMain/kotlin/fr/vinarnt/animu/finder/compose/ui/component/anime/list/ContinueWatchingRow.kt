package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.EpisodeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.resolveAnimeTitle
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.ContinueWatchingItem

private val CardWidth = 200.dp

@Composable
fun ContinueWatchingRow(
    items: List<ContinueWatchingItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = strings.home.continueWatching,
            style = MaterialTheme.typography.titleMedium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(items, key = { it.entry.animeId }) { item ->
                val anime = item.anime
                if (anime == null) {
                    Surface(
                        modifier = Modifier
                            .width(CardWidth)
                            .aspectRatio(3f / 4f),
                        shape = RoundedCornerShape(CornerRadius.md),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = Elevation.sm,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.sm))
                        }
                    }
                } else {
                    AnimeCard(
                        modifier = Modifier.width(CardWidth),
                        malId = anime.malId ?: item.entry.animeId,
                        title = resolveAnimeTitle(anime),
                        thumbnailUrl = anime.images?.webp?.largeImageUrl
                            ?: anime.images?.jpg?.largeImageUrl ?: "",
                        badgeText = "EP ${item.entry.episodeNumber}",
                        onClick = {
                            navigator.push(
                                EpisodeDetailScreen(
                                    animeId = item.entry.animeId,
                                    episodeNumber = item.entry.episodeNumber,
                                    animeTitle = resolveAnimeTitle(anime),
                                    altTitles = anime.titles?.mapNotNull { it.title }.orEmpty(),
                                    totalEpisodes = anime.episodes,
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

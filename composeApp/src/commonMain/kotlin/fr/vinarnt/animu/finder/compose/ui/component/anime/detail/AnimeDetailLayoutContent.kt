package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.EpisodeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import fr.vinarnt.jikan4k.models.Anime
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyColumn
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnimeDetailLayoutContent(modifier: Modifier = Modifier, lazyListState: LazyListState) {
    val vm: AnimeDetailViewModel = koinViewModel()
    val episodesPaginationState by vm.episodesPaginationState.collectAsStateWithLifecycle()
    val anime by vm.anime.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.currentOrThrow

    key(episodesPaginationState) {
        PaginatedLazyColumn(
            paginationState = episodesPaginationState,
            modifier = modifier.fillMaxSize().padding(horizontal = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            state = lazyListState,
            firstPageProgressIndicator = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.md))
                }
            },
            newPageProgressIndicator = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.sm))
                }
            },
            newPageErrorIndicator = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { episodesPaginationState.retryLastFailedRequest() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                    }
                }
            }
        ) {
            val episodes = episodesPaginationState.allItems ?: emptyList()
            items(
                count = episodes.size,
                key = { index -> "episode:${episodes[index].malId ?: index}" }
            ) { index ->
                val episode = episodes[index]
                EpisodeItem(
                    episode = episode,
                    onClick = {
                        val a = anime
                        val malId = a?.malId
                        val episodeNumber = episode.malId
                        if (a != null && malId != null && episodeNumber != null) {
                            navigator.push(
                                EpisodeDetailScreen(
                                    animeId = malId,
                                    episodeNumber = episodeNumber,
                                    animeTitle = resolveAnimeTitle(a),
                                    altTitles = a.titles?.mapNotNull { it.title }.orEmpty(),
                                    totalEpisodes = a.episodes,
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

fun resolveAnimeTitle(anime: Anime): String =
    anime.titles?.firstOrNull { it.type in listOf("English", "Default") }?.title
        ?: anime.titles?.firstOrNull()?.title
        ?: "Unknown"
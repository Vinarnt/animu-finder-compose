package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.EpisodeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.theme.Breakpoints
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import org.koin.compose.viewmodel.koinViewModel

/** The episodes section of the anime detail page: header, grid/list toggle and pagination. */
@Composable
fun AnimeDetailLayoutContent(modifier: Modifier = Modifier, lazyListState: LazyListState) {
    val vm: AnimeDetailViewModel = koinViewModel()
    val episodesPaginationState by vm.episodesPaginationState.collectAsStateWithLifecycle()
    val anime by vm.anime.collectAsStateWithLifecycle()
    val episodeDisplay by vm.episodeDisplay.collectAsStateWithLifecycle()
    val watchHistory by vm.watchHistory.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.currentOrThrow

    val activeEpisodeNumber = anime?.malId?.let { malId ->
        watchHistory.firstOrNull { it.animeId == malId }?.episodeNumber
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val compact = maxWidth < Breakpoints.compactMaxWidth
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        val gutter = sidePadding + if (compact) 16.dp else 24.dp

        Column(modifier = Modifier.fillMaxSize()) {
            EpisodeSectionHeader(
                title = anime?.episodes?.let { "${strings.animeDetail.episodesLabel} · $it" }
                    ?: strings.animeDetail.episodesLabel,
                selected = episodeDisplay,
                onSelect = { vm.setEpisodeDisplay(it) },
                gutter = gutter,
            )

            Box(modifier = Modifier.weight(1f)) {
                key(episodesPaginationState, episodeDisplay) {
                    val episodes = episodesPaginationState.allItems ?: emptyList()
                    val unknownFallback = strings.common.unknown
                    val onEpisodeClick: (GetAnimeByIdEpisodes200ResponseDataInner) -> Unit = { episode ->
                        val a = anime
                        val malId = a?.malId
                        val episodeNumber = episode.malId
                        if (a != null && malId != null && episodeNumber != null) {
                            navigator.push(
                                EpisodeDetailScreen(
                                    animeId = malId,
                                    episodeNumber = episodeNumber,
                                    animeTitle = resolveAnimeTitle(a, unknownFallback),
                                    altTitles = a.titles?.mapNotNull { it.title }.orEmpty(),
                                    totalEpisodes = a.episodes,
                                )
                            )
                        }
                    }

                    if (episodeDisplay == EpisodeDisplay.GRID) {
                        EpisodeGrid(
                            paginationState = episodesPaginationState,
                            episodes = episodes,
                            compact = compact,
                            gutter = gutter,
                            activeEpisodeNumber = activeEpisodeNumber,
                            onEpisodeClick = onEpisodeClick,
                        )
                    } else {
                        EpisodeColumn(
                            paginationState = episodesPaginationState,
                            episodes = episodes,
                            episodeDisplay = episodeDisplay,
                            gutter = gutter,
                            activeEpisodeNumber = activeEpisodeNumber,
                            onEpisodeClick = onEpisodeClick,
                            lazyListState = lazyListState,
                        )
                    }
                }
            }
        }
    }
}

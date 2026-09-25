package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.EpisodeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.component.BookmarkIcon
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.HomeHeader
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.HomeShelfSpec
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.button.GlassIconButton
import fr.vinarnt.animu.finder.compose.ui.component.button.SettingButton
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import fr.vinarnt.animu.finder.compose.viewmodel.HomeShelvesViewModel
import fr.vinarnt.jikan4k.apis.AnimeApi
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel


class AnimeListScreen : Screen {

    override val key: ScreenKey = "anime:list"

    @Composable
    override fun Content() {
        val home = strings.home
        val vm: AnimeListViewModel = koinViewModel()
        val shelvesVm: HomeShelvesViewModel = koinViewModel()
        val paginationState by vm.paginationState.collectAsStateWithLifecycle()
        val resumeItems by vm.resumeItems.collectAsStateWithLifecycle()
        val myList by vm.myList.collectAsStateWithLifecycle()
        val trendingItems by shelvesVm.trending.collectAsStateWithLifecycle()
        val newEpisodesItems by shelvesVm.newEpisodes.collectAsStateWithLifecycle()
        val topRatedItems by shelvesVm.topRated.collectAsStateWithLifecycle()
        val actionItems by shelvesVm.action.collectAsStateWithLifecycle()
        val fantasyItems by shelvesVm.fantasy.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val unknownFallback = strings.common.unknown

        val gridState = rememberLazyGridState()
        var scrolled by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            shelvesVm.ensureLoaded()
        }

        LaunchedEffect(gridState) {
            snapshotFlow {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
            }
                .distinctUntilChanged()
                .collect { collapsed -> scrolled = collapsed }
        }

        MainLayout(
            topBar = {
                NavigationBar(
                    brand = strings.navigation.appName,
                    overlay = true,
                    scrolled = scrolled,
                ) {
                    GlassIconButton(
                        imageVector = BookmarkIcon,
                        contentDescription = strings.myList.title,
                        onClick = { navigator.push(MyListScreen()) },
                        onGlass = !scrolled,
                    )
                    SettingButton(onGlass = !scrolled)
                }
            },
            overlayTopBar = true,
            scrollable = false
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f)
                    .coerceAtLeast(0.dp)
                key(paginationState) {
                    PaginatedLazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(1),
                        state = gridState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            end = 0.dp,
                            top = 0.dp,
                            bottom = Spacing.md
                        ),
                        paginationState = paginationState,
                        firstPageProgressIndicator = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.md))
                            }
                        },
                        newPageProgressIndicator = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.md))
                            }
                        },
                        newPageErrorIndicator = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { paginationState.retryLastFailedRequest() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = strings.common.retry)
                                }
                            }
                        }
                    ) {
                        item(key = "home:header", span = { GridItemSpan(maxLineSpan) }) {
                            HomeHeader(
                                loadedItems = paginationState.allItems.orEmpty(),
                                resumeItems = resumeItems,
                                myList = myList,
                                shelves = listOf(
                                    HomeShelfSpec(
                                        title = home.trending,
                                        items = trendingItems,
                                        onSeeAll = {
                                            navigator.push(
                                                BrowseScreen(orderBy = AnimeApi.OrderByGetAnime.MEMBERS)
                                            )
                                        },
                                    ),
                                    HomeShelfSpec(
                                        title = home.newEpisodes,
                                        items = newEpisodesItems,
                                        onSeeAll = {
                                            navigator.push(
                                                BrowseScreen(orderBy = AnimeApi.OrderByGetAnime.START_DATE)
                                            )
                                        },
                                    ),
                                    HomeShelfSpec(
                                        title = home.topRated,
                                        items = topRatedItems,
                                        onSeeAll = {
                                            navigator.push(
                                                BrowseScreen(orderBy = AnimeApi.OrderByGetAnime.SCORE)
                                            )
                                        },
                                    ),
                                    HomeShelfSpec(
                                        title = home.action,
                                        items = actionItems,
                                        onSeeAll = {
                                            navigator.push(
                                                BrowseScreen(initialGenres = setOf(AnimeGenre.ACTION))
                                            )
                                        },
                                    ),
                                    HomeShelfSpec(
                                        title = home.fantasy,
                                        items = fantasyItems,
                                        onSeeAll = {
                                            navigator.push(
                                                BrowseScreen(initialGenres = setOf(AnimeGenre.FANTASY))
                                            )
                                        },
                                    ),
                                ),
                                onPlay = { anime ->
                                    navigator.push(
                                        EpisodeDetailScreen(
                                            animeId = anime.malId ?: 0,
                                            episodeNumber = 1,
                                            animeTitle = anime.title ?: unknownFallback,
                                            altTitles = anime.titles?.mapNotNull { it.title }.orEmpty(),
                                            totalEpisodes = anime.episodes,
                                        )
                                    )
                                },
                                onToggleMyList = { anime ->
                                    vm.toggleMyList(anime.malId ?: 0)
                                },
                                onOpenBrowse = { navigator.push(BrowseScreen()) },
                                onSearch = { query ->
                                    navigator.push(BrowseScreen(initialQuery = query))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = sidePadding + Spacing.md),
                            )
                        }
                    }
                }
            }
        }
    }
}

package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.BrowseEmptyResults
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.BrowseHeader
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.base.rememberDebouncedCallback
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters
import fr.vinarnt.jikan4k.apis.AnimeApi
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import org.koin.compose.viewmodel.koinViewModel

class BrowseScreen(
    private val initialQuery: String = "",
    private val initialGenres: Set<AnimeGenre> = emptySet(),
    private val orderBy: AnimeApi.OrderByGetAnime = AnimeApi.OrderByGetAnime.SCORE,
) : Screen {

    override val key: ScreenKey = "anime:browse"

    @Composable
    override fun Content() {
        val vm: AnimeListViewModel = koinViewModel()
        val filters by vm.filters.collectAsStateWithLifecycle()
        val paginationState by vm.paginationState.collectAsStateWithLifecycle()

        val debouncedApply = rememberDebouncedCallback { vm.applyCurrentFilters() }
        val gridState = rememberLazyGridState()
        val unknownFallback = strings.common.unknown
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!initialized) {
                initialized = true
                vm.initializeFilters(
                    AnimeSearchFilters(
                        queryText = initialQuery,
                        genres = initialGenres,
                        orderBy = orderBy,
                    )
                )
            }
        }

        val clearFilters: () -> Unit = {
            vm.updateQuery("")
            vm.updateType(null)
            vm.updateStatus(null)
            vm.updateRating(null)
            vm.updateGenres(emptySet())
            vm.updateScoreRange(1f..10f)
            vm.setOrderBy(AnimeApi.OrderByGetAnime.SCORE)
            vm.setSort(AnimeApi.SortGetAnime.DESC)
            debouncedApply.flush()
        }

        MainLayout(
            topBar = {
                NavigationBar(title = strings.animeList.browseTitle)
            },
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
                        columns = GridCells.Adaptive(150.dp),
                        state = gridState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(
                            start = sidePadding + Spacing.md,
                            end = sidePadding + Spacing.md,
                            top = Spacing.md,
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
                        item(key = "browse:header", span = { GridItemSpan(maxLineSpan) }) {
                            BrowseHeader(
                                filters = filters,
                                resultCount = paginationState.allItems?.size,
                                onClearFilters = clearFilters,
                                onQueryChange = {
                                    vm.updateQuery(it)
                                    debouncedApply()
                                },
                                onTypeChange = {
                                    debouncedApply.cancel()
                                    vm.updateType(it)
                                },
                                onStatusChange = {
                                    debouncedApply.cancel()
                                    vm.updateStatus(it)
                                },
                                onRatingChange = {
                                    debouncedApply.cancel()
                                    vm.updateRating(it)
                                },
                                onGenresChange = {
                                    debouncedApply.cancel()
                                    vm.updateGenres(it)
                                },
                                onScoreRangeChange = {
                                    vm.updateScoreRange(it)
                                    debouncedApply()
                                },
                                onScoreRangeCommit = {
                                    debouncedApply.flush()
                                },
                                onSortChange = { orderBy, sort ->
                                    debouncedApply.cancel()
                                    vm.setOrderBy(orderBy)
                                    vm.setSort(sort)
                                    debouncedApply()
                                }
                            )
                        }

                        itemsIndexed(
                            paginationState.allItems ?: emptyList(),
                            key = { index, _ -> "browse:card:$index" }
                        ) { _, anime ->
                            AnimeCard(
                                title = anime.titles?.firstOrNull()?.title ?: unknownFallback,
                                malId = anime.malId ?: 0,
                                thumbnailUrl = anime.images?.jpg?.imageUrl
                                    ?: anime.images?.webp?.imageUrl ?: "",
                                score = anime.score,
                                subtitle = buildList {
                                    anime.type?.let { add(it) }
                                    anime.year?.let { add(it.toString()) }
                                }.joinToString(" · ").ifEmpty { null },
                            )
                        }

                        val loadedItems = paginationState.allItems
                        if (loadedItems != null && loadedItems.isEmpty()) {
                            item(key = "browse:empty", span = { GridItemSpan(maxLineSpan) }) {
                                BrowseEmptyResults(onClear = clearFilters)
                            }
                        }
                    }
                }
            }
        }
    }
}

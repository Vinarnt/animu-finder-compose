package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.AnimeListFilters
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.ContinueWatchingRow
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.base.rememberDebouncedCallback
import fr.vinarnt.animu.finder.compose.ui.component.button.SettingButton
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

class AnimeListScreen : Screen {

    override val key: ScreenKey = "anime:list"

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val vm: AnimeListViewModel = koinViewModel()
        val filters by vm.filters.collectAsStateWithLifecycle()
        val paginationState by vm.paginationState.collectAsStateWithLifecycle()
        val resumeItems by vm.resumeItems.collectAsStateWithLifecycle()

        val debouncedApply = rememberDebouncedCallback { vm.applyCurrentFilters() }

        val gridState = rememberLazyGridState()
        var rowCollapsed by remember { mutableStateOf(false) }

        LaunchedEffect(gridState) {
            snapshotFlow {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
            }
                .distinctUntilChanged()
                .collect { collapsed -> rowCollapsed = collapsed }
        }

        MainLayout(
            topBar = {
                NavigationBar(title = "App title") {
                    SettingButton()
                }
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
                        columns = GridCells.Adaptive(200.dp),
                        state = gridState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = sidePadding + Spacing.sm,
                            end = sidePadding + Spacing.sm,
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
                                Icon(Icons.Default.Refresh, "Retry")
                            }
                        }
                    }
                ) {
                    item(key = "anime:list:header", span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            AnimatedVisibility(
                                visible = !rowCollapsed,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                ContinueWatchingRow(items = resumeItems)
                            }

                            AnimeListFilters(
                                filters = filters,
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
                                }
                            )
                        }
                    }

                    itemsIndexed(
                        paginationState.allItems ?: emptyList(),
                        key = { index, _ -> "anime:list:card:$index" }
                    ) { _, anime ->
                        AnimeCard(
                            title = anime.titles?.firstOrNull()?.title ?: "Unknown",
                            malId = anime.malId ?: 0,
                            thumbnailUrl = anime.images?.jpg?.imageUrl
                                ?: anime.images?.webp?.imageUrl ?: "",
                            score = anime.score,
                        )
                    }
                }
            }
        }
    }
}
}
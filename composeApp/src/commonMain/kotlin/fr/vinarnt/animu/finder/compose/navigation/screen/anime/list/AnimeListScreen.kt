package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.ui.component.anime.list.AnimeListFilters
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.button.SettingButton
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import org.koin.compose.viewmodel.koinViewModel

class AnimeListScreen : Screen {

    override val key: ScreenKey = "anime:list"

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val vm: AnimeListViewModel = koinViewModel()
        val filters by vm.filters.collectAsStateWithLifecycle()
        val paginationState by vm.paginationState.collectAsStateWithLifecycle()

        MainLayout(topBar = {
            NavigationBar(title = "App title") {
                SettingButton()
            }
        }) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimeListFilters(
                    filters = filters,
                    onQueryChange = { vm.updateQuery(it) },
                    onTypeChange = { vm.updateType(it) },
                    onStatusChange = { vm.updateStatus(it) },
                    onRatingChange = { vm.updateRating(it) },
                    onGenresChange = { vm.updateGenres(it) },
                    onScoreRangeChange = { vm.updateScoreRange(it) },
                    onScoreRangeCommit = { vm.commitScoreRange() }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    key(paginationState) {
                        PaginatedLazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Adaptive(200.dp),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.md))
                                }
                            },
                            newPageErrorIndicator = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = { paginationState.retryLastFailedRequest() }) {
                                        Icon(Icons.Default.Refresh, "Retry")
                                    }
                                }
                            }
                        ) {
                            itemsIndexed(
                                paginationState.allItems ?: emptyList(),
                                key = { index, _ -> "anime:list:card:$index" }
                            ) { _, anime ->
                                AnimeCard(
                                    title = anime.titles?.first()?.title!!,
                                    malId = anime.malId!!,
                                    thumbnailUrl = anime.images?.jpg?.imageUrl!!,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
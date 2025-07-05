package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.button.SettingButton
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import org.koin.compose.viewmodel.koinViewModel

class AnimeListScreen : Screen {

    override val key: ScreenKey = "anime:list"

    @Composable
    override fun Content() {
        val vm: AnimeListViewModel = koinViewModel()

        val paginationState = vm.paginationState

        MainLayout(topBar = {
            NavigationBar(
                title = "App title"
            ) {
                SettingButton()
            }
        }) {
            PaginatedLazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                paginationState = paginationState,
                firstPageProgressIndicator = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                },
                newPageProgressIndicator = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                },
                newPageErrorIndicator = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                paginationState.retryLastFailedRequest()
                            }
                        ) {
                            Icon(Icons.Default.Refresh, "Retry")
                        }
                    }
                }
            ) {
                itemsIndexed(
                    paginationState.allItems!!,
                    key = { index, anime -> "anime:list:card:$index" }
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

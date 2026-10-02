package fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.AnimeDetailHero
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.AnimeDetailLayout
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.resolveAnimeTitle
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

class AnimeDetailScreen(private val malId: Int) : Screen {

    override val key: ScreenKey = "anime:detail"

    @Composable
    override fun Content() {
        val vm: AnimeDetailViewModel = koinViewModel()
        val anime by vm.anime.collectAsStateWithLifecycle()
        val myList by vm.myList.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow
        val unknownFallback = strings.common.unknown
        var scrolled by remember { mutableStateOf(false) }

        LaunchedEffect(malId) {
            vm.getAnime(malId)
        }

        MainLayout(
            topBar = {
                NavigationBar(
                    title = anime?.let { resolveAnimeTitle(it, unknownFallback) },
                    overlay = true,
                    scrolled = scrolled,
                )
            },
            scrollable = false,
            overlayTopBar = true
        ) {
            if (anime == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val a = anime!!
                AnimeDetailLayout(
                    anime = a,
                    onScrolledChange = { scrolled = it },
                    heroContent = {
                        AnimeDetailHero(
                            anime = a,
                            onWatch = {
                                navigator.push(
                                    EpisodeDetailScreen(
                                        animeId = malId,
                                        episodeNumber = 1,
                                        animeTitle = resolveAnimeTitle(a, unknownFallback),
                                        altTitles = a.titles?.mapNotNull { it.title }.orEmpty(),
                                        totalEpisodes = a.episodes,
                                    )
                                )
                            },
                            watchLabel = strings.player.play,
                            inMyList = malId in myList,
                            onToggleMyList = {
                                vm.toggleMyList()
                            },
                        )
                    },
                )
            }
        }
    }
}

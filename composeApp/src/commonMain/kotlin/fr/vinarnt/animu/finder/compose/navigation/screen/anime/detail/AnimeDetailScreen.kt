package fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.AnimeDetailLayout
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.button.SettingButton
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

class AnimeDetailScreen(private val malId: Int) : Screen {

    override val key: ScreenKey = "anime:detail"

    @Composable
    override fun Content() {
        val vm: AnimeDetailViewModel = koinViewModel()
        val anime by vm.anime.collectAsStateWithLifecycle()

        LaunchedEffect(malId) {
            vm.getAnime(malId)
        }

        MainLayout(
            topBar = {
                NavigationBar() {
                    SettingButton()
                }
            },
            scrollable = false
        ) {
            if (anime == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AnimeDetailLayout(anime!!)
            }
        }
    }
}


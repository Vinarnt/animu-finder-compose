package fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
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
        val title = anime?.titles?.first { it.type == "English" }?.title ?: ""

        LaunchedEffect(malId) {
            vm.getAnime(malId)
        }

        MainLayout(
            topBar = {
                NavigationBar(title = title) {
                    SettingButton()
                }
            }
        ) {
            if (anime == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(4.dp)
                    ) {
                        Text(text = "Mal ID: $malId")
                        Text(text = "Status: ${anime?.status}")
                        Text(text = "Episodes: ${anime?.episodes}")
                        Text(text = "Score: ${anime?.score}")
                        Text(text = "Rank: ${anime?.rank}")
                        Text(text = "Popularity: ${anime?.popularity}")
                        Text(text = "Members: ${anime?.members}")
                        Text(text = "Favorites: ${anime?.favorites}")
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp, 0.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(1000) { index ->
                            Box(
                                modifier = Modifier.fillMaxWidth().height(30.dp)
                                    .background(color = MaterialTheme.colorScheme.tertiaryContainer).padding(4.dp)
                            ) {
                                Text(text = "Item $index")
                            }
                        }
                    }
                }
            }
        }
    }
}

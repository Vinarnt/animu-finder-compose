package fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.repository.extractor.EpisodeRef
import fr.vinarnt.animu.finder.compose.repository.extractor.EpisodeSearchQuery
import fr.vinarnt.animu.finder.compose.ui.component.anime.detail.EpisodeDetailLayout
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.component.player.PlayerFullscreenEffect
import fr.vinarnt.animu.finder.compose.ui.component.player.StreamingVideoPlayer
import fr.vinarnt.animu.finder.compose.viewmodel.EpisodeDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

class EpisodeDetailScreen(
    private val animeId: Int,
    private val episodeNumber: Int,
    private val animeTitle: String,
    private val altTitles: List<String>,
    private val totalEpisodes: Int? = null,
) : Screen {

    override val key: ScreenKey = "anime:episode"

    @Composable
    override fun Content() {
        val vm: EpisodeDetailViewModel = koinViewModel(key = "episode:$animeId:$episodeNumber")
        val episode by vm.episode.collectAsStateWithLifecycle()
        val streams by vm.streams.collectAsStateWithLifecycle()
        val streamErrors by vm.streamErrors.collectAsStateWithLifecycle()
        val selectedStream by vm.selectedStream.collectAsStateWithLifecycle()
        val loadingStreams by vm.loadingStreams.collectAsStateWithLifecycle()

        var isFullscreen by remember { mutableStateOf(false) }
        val stream = selectedStream
        val movablePlayer = remember {
            movableContentOf<StreamSource> { source ->
                StreamingVideoPlayer(
                    source = source,
                    isFullscreen = isFullscreen,
                    onFullscreenChange = { isFullscreen = it },
                )
            }
        }

        LaunchedEffect(animeId, episodeNumber) {
            vm.loadEpisode(animeId, episodeNumber)
            vm.loadStreams(
                EpisodeSearchQuery(
                    animeTitle = animeTitle,
                    altTitles = altTitles,
                    episode = EpisodeRef(absolute = episodeNumber),
                    totalEpisodes = totalEpisodes,
                )
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MainLayout(
                topBar = {
                    NavigationBar(title = "Episode $episodeNumber")
                }
            ) {
                EpisodeDetailLayout(
                    episode = episode,
                    episodeNumber = episodeNumber,
                    streams = streams,
                    streamErrors = streamErrors,
                    selectedStream = selectedStream,
                    loadingStreams = loadingStreams,
                    onSelectStream = vm::selectStream,
                    isFullscreen = isFullscreen,
                    playerContent = {
                        if (stream != null) {
                            movablePlayer(stream)
                        }
                    },
                )
            }

            if (isFullscreen && stream != null) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .focusRequester(focusRequester)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                isFullscreen = false
                                true
                            } else {
                                false
                            }
                        },
                ) {
                    movablePlayer(stream)
                }
            }

            PlayerFullscreenEffect(isFullscreen = isFullscreen) { isFullscreen = false }
        }
    }
}

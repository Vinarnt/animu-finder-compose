package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.model.StreamSource

// Height reserved for the bottom control bar. The tap-to-play overlay and the
// subtitle overlay both leave this strip untouched so the controls keep receiving
// their own pointer events and subtitles never overlap them.
internal val ControlBarHeight = 104.dp

@Composable
expect fun StreamingVideoPlayer(
    source: StreamSource,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onFullscreenChange: (Boolean) -> Unit = {},
)
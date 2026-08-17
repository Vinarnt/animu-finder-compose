package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import fr.vinarnt.animu.finder.compose.model.StreamSource

@Composable
fun StreamingVideoPlayer(
    source: StreamSource,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onFullscreenChange: (Boolean) -> Unit = {},
) {
    val playerHost = remember(source.url) {
        MediaPlayerHost(
            mediaUrl = source.url,
            autoPlay = false,
            headers = source.headers.ifEmpty { null },
        )
    }

    val currentOnFullscreenChange by rememberUpdatedState(onFullscreenChange)

    LaunchedEffect(playerHost) {
        playerHost.onEvent = { event ->
            if (event is MediaPlayerEvent.FullScreenChange) {
                currentOnFullscreenChange(event.isFullScreen)
            }
        }
    }

    // Keep the host's fullscreen flag in sync with the hoisted fullscreen state,
    // so exiting fullscreen by another path (e.g. ESC) also resets the player icon.
    LaunchedEffect(isFullscreen) {
        playerHost.setFullScreen(isFullscreen)
    }

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .hoverable(interaction),
    ) {
        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                isAutoHideControlEnabled = false,
                showControlsOverride = hovered,
                seekBarThumbColor = colors.primary,
                seekBarActiveTrackColor = colors.primary,
                seekBarInactiveTrackColor = colors.surfaceVariant,
                durationTextColor = colors.onSurface,
                iconsTintColor = colors.onSurface,
                loadingIndicatorColor = colors.primary,
            ),
        )
    }
}

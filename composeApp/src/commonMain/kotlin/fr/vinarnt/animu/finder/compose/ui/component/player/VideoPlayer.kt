package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import fr.vinarnt.animu.finder.compose.model.StreamSource
import kotlinx.coroutines.delay

private val ControlBarHeight = 104.dp
private const val SeekStepSeconds = 10f

/**
 * Handles the player's keyboard shortcuts. Shared between the inline player and
 * the fullscreen overlay so the shortcuts work in both modes.
 */
internal fun MediaPlayerHost.handlePlayerKey(
    event: KeyEvent,
    currentTime: Float,
    totalTime: Float,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when (event.key) {
        Key.Spacebar -> {
            togglePlayPause()
            true
        }
        Key.DirectionLeft -> {
            seekTo((currentTime - SeekStepSeconds).coerceAtLeast(0f))
            true
        }
        Key.DirectionRight -> {
            seekTo((currentTime + SeekStepSeconds).coerceAtMost(totalTime))
            true
        }
        else -> false
    }
}

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

    var currentTime by remember { mutableStateOf(0f) }
    var totalTime by remember { mutableStateOf(0f) }

    LaunchedEffect(playerHost) {
        playerHost.onEvent = { event ->
            when (event) {
                is MediaPlayerEvent.FullScreenChange -> currentOnFullscreenChange(event.isFullScreen)
                is MediaPlayerEvent.CurrentTimeChange -> currentTime = event.currentTime
                is MediaPlayerEvent.TotalTimeChange -> totalTime = event.totalTime
                else -> Unit
            }
        }
    }

    // Keep the host's fullscreen flag in sync with the hoisted fullscreen state,
    // so exiting fullscreen by another path (e.g. ESC) also resets the player icon.
    LaunchedEffect(isFullscreen) {
        playerHost.setFullScreen(isFullscreen)
    }

    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    // Controls are only shown while hovering, and hide shortly after the mouse
    // leaves the player. The library's click handler never shows/hides them.
    var controlsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(hovered) {
        if (hovered) {
            controlsVisible = true
        } else {
            delay(800)
            controlsVisible = false
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .hoverable(interaction)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                playerHost.handlePlayerKey(event, currentTime, totalTime)
            },
    ) {
        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                // Visibility is driven by the hover override below; the library
                // must not auto-hide on its own, and clicks must not toggle it.
                isAutoHideControlEnabled = false,
                showControlsOverride = controlsVisible,
                // Fire control actions immediately: the library delays every
                // button click by controlClickAnimationDuration (default 300ms)
                // and drops clicks landing during that window.
                controlClickAnimationDuration = 0,
                seekBarThumbColor = colors.primary,
                seekBarActiveTrackColor = colors.primary,
                seekBarInactiveTrackColor = colors.surfaceVariant,
                durationTextColor = colors.onSurface,
                iconsTintColor = colors.onSurface,
                loadingIndicatorColor = colors.primary,
            ),
        )

        // Transparent overlay over the video area (the bottom strip is left for
        // the built-in control bar): single tap toggles play/pause, double tap
        // toggles fullscreen. Consuming these events also keeps the library's own
        // click-to-toggle-controls from firing over the video area.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = ControlBarHeight)
                .pointerInput(playerHost) {
                    detectTapGestures(
                        onTap = { playerHost.togglePlayPause() },
                        onDoubleTap = { playerHost.toggleFullScreen() },
                    )
                }
        )
    }
}
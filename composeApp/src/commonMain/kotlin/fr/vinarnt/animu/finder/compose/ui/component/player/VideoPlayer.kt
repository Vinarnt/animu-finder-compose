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
import androidx.compose.runtime.key
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

internal val ControlBarHeight = 104.dp

// The library's selection menus (quality/speed/audio/subtitles) slide in from the
// right edge, keeping a ~35dp margin around their buttons. The tap overlay below
// reserves this strip so those menus keep receiving their own taps instead of the
// overlay toggling play/pause.
internal val SideMenuStripWidth = 200.dp

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
    return event.type == KeyEventType.KeyDown && when (event.key) {
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
    // Key the whole player subtree on the media URL: the underlying platform
    // player (VLC) keeps its event listener bound to the first MediaPlayerHost
    // it sees, so switching sources without recreating it leaves the new host
    // without duration updates (no seek bar). Recreating the subtree per URL
    // pairs each host with its own player instance.
    key(source.url) {
        val playerHost = remember {
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
                    // Some providers deliver single-audio streams (e.g. AniNeko
                    // packer HLS served as ".txt") where the library's audio-track
                    // detection surfaces spurious duplicates. They opt out per stream
                    // via StreamSource.supportsAudioTrackSelection, so the selector is
                    // hidden only when the stream actually benefits from it.
                    showAudioTracksOptions = source.supportsAudioTrackSelection,
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

            // Interaction overlay over the video area. The bottom strip is left for the
            // built-in control bar, and the right strip for the library's side
            // selection menus, so both stay fully interactible. A tap toggles
            // play/pause, a double tap toggles fullscreen. The subtitle overlay
            // renders as a child of this box, so taps on the subtitle text fall
            // through to this gesture while drags on it move the subtitle.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = ControlBarHeight, end = SideMenuStripWidth)
                    .pointerInput(playerHost) {
                        detectTapGestures(
                            onTap = { playerHost.togglePlayPause() },
                            onDoubleTap = { playerHost.toggleFullScreen() },
                        )
                    },
            ) {
                SubtitleOverlay(
                    subtitles = source.subtitles,
                    currentTime = currentTime,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
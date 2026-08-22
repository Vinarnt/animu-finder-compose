package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.StreamSource
import kotlinx.coroutines.delay
import org.openani.mediamp.MediaLoadCancellationException
import org.openani.mediamp.PlaybackException
import org.openani.mediamp.compose.MediampPlayerSurface
import org.openani.mediamp.compose.rememberMediampPlayer
import org.openani.mediamp.errorOrNull
import org.openani.mediamp.isLoadingOrBuffering
import org.openani.mediamp.source.UriMediaData
import org.openani.mediamp.togglePlayWhenReady

// Height reserved for the bottom control bar. The tap-to-play overlay and the
// subtitle overlay both leave this strip untouched so the controls keep receiving
// their own pointer events and subtitles never overlap them.
internal val ControlBarHeight = 104.dp

private const val SeekStepMillis = 10_000L

/**
 * Streaming video player backed by MediaMP (mediamp). It owns a single player
 * instance per media URL (the whole subtree is keyed on [StreamSource.url] so a
 * source switch creates a fresh player) and renders the video surface plus a
 * hand-built control bar: play/pause, seek, time, playback speed, audio-track
 * selection and fullscreen.
 *
 * MediaMP only provides the bare video surface, so all controls are composed here
 * on top of it. The public signature mirrors the previous CMMP-based player so the
 * call sites stay unchanged.
 */
@Composable
fun StreamingVideoPlayer(
    source: StreamSource,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onFullscreenChange: (Boolean) -> Unit = {},
) {
    key(source.url) {
        val player = rememberMediampPlayer()

        val currentOnFullscreenChange by rememberUpdatedState(onFullscreenChange)
        val currentIsFullscreen by rememberUpdatedState(isFullscreen)

        // Load the media paused (the previous player did not autoplay). Opening
        // failures are surfaced through player.state (MediaStatus.Error); the thrown
        // exception is only used for control flow, so it is swallowed here.
        LaunchedEffect(player, source.url, source.headers) {
            try {
                player.setMediaData(
                    data = UriMediaData(source.url, source.headers),
                    playWhenReady = false,
                )
            } catch (_: MediaLoadCancellationException) {
                // Superseded by a newer load or the player was closed.
            } catch (_: PlaybackException) {
                // Handled via the error state below.
            }
        }

        val playerState by player.state.collectAsStateWithLifecycle()
        val positionMs by player.currentPositionMillis.collectAsStateWithLifecycle()
        val properties by player.mediaProperties.collectAsStateWithLifecycle()

        val durationMs = properties?.durationMillis ?: 0L
        val isPlaying = playerState.playWhenReady
        val loading = playerState.isLoadingOrBuffering
        val error = playerState.errorOrNull

        val interaction = remember { MutableInteractionSource() }
        val hovered by interaction.collectIsHoveredAsState()

        // Controls are shown while hovering, and are pinned while the player is not
        // actively playing (paused, loading or errored) so the user always has a way
        // to resume. When the mouse leaves an actively-playing video they fade out.
        var controlsVisible by remember { mutableStateOf(false) }
        val pinned = !isPlaying || loading || error != null
        LaunchedEffect(hovered, pinned) {
            controlsVisible = true
            if (!hovered && !pinned) {
                delay(1200)
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
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Spacebar -> {
                            player.togglePlayWhenReady()
                            true
                        }
                        Key.DirectionLeft -> {
                            player.seekTo((positionMs - SeekStepMillis).coerceAtLeast(0L))
                            true
                        }
                        Key.DirectionRight -> {
                            player.seekTo((positionMs + SeekStepMillis).coerceAtMost(durationMs))
                            true
                        }
                        Key.F -> {
                            currentOnFullscreenChange(!currentIsFullscreen)
                            true
                        }
                        else -> false
                    }
                },
        ) {
            MediampPlayerSurface(
                mediampPlayer = player,
                modifier = Modifier.fillMaxSize(),
            )

            // Interaction overlay over the video area. The bottom strip is left for
            // the control bar so it stays fully interactible. A tap toggles
            // play/pause (and reveals the controls), a double tap toggles fullscreen.
            // The subtitle overlay renders as a child of this box, so taps on the
            // subtitle text fall through to this gesture while drags on it move it.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = ControlBarHeight)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                player.togglePlayWhenReady()
                                controlsVisible = true
                            },
                            onDoubleTap = { currentOnFullscreenChange(!currentIsFullscreen) },
                        )
                    },
            ) {
                SubtitleOverlay(
                    subtitles = source.subtitles,
                    currentTime = positionMs / 1000f,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (error != null) {
                Text(
                    text = strings.player.error,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AnimatedVisibility(
                visible = controlsVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                PlayerControlBar(
                    player = player,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    isFullscreen = isFullscreen,
                    showAudioSelector = source.supportsAudioTrackSelection,
                    onTogglePlayPause = { player.togglePlayWhenReady() },
                    onSeek = { player.seekTo(it) },
                    onToggleFullscreen = { currentOnFullscreenChange(!currentIsFullscreen) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

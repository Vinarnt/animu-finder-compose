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
import androidx.compose.ui.layout.ContentScale
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.StreamSource
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay

private const val SeekStepMillis = 10_000L

/**
 * Streaming video player backed by ComposeMediaPlayer. One player instance per media
 * URL: the subtree is keyed on [StreamSource.url], so switching source builds a fresh
 * player. Renders the video surface plus a hand-built control bar.
 *
 * ComposeMediaPlayer decodes natively (GStreamer on Linux, platform bridges elsewhere)
 * but draws each frame into a Compose [androidx.compose.foundation.Canvas], so unlike
 * mediamp's desktop backend it needs neither AWT/SkiaLayer nor the host window's Skia
 * `DirectContext` — which is what lets it run inside a Nucleus Tao window.
 *
 * The library also has its own `toggleFullscreen()`; it is deliberately not used here.
 * Its fullscreen overlay opens an `androidx.compose.ui.window.Window` (AWT), which Tao
 * cannot host, and the Linux surface stops painting while `isFullscreen` is set. The
 * app already drives fullscreen through [onFullscreenChange], so the player leaves
 * `VideoPlayerState.isFullscreen` alone and lets the caller move the surface.
 */
@Composable
actual fun StreamingVideoPlayer(
    source: StreamSource,
    modifier: Modifier,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
) {
    key(source.url) {
        val player = rememberVideoPlayerState()

        val currentOnFullscreenChange by rememberUpdatedState(onFullscreenChange)
        val currentIsFullscreen by rememberUpdatedState(isFullscreen)

        // Load the media paused (no autoplay). Playback failures surface through
        // player.error below. Note: the native backends take a bare URI, so provider
        // headers (Referer/User-Agent) cannot be forwarded yet.
        LaunchedEffect(player, source.url) {
            player.openUri(source.url, InitialPlayerState.PAUSE)
        }

        val isPlaying = player.isPlaying
        val loading = player.isLoading || (!player.hasMedia && player.error == null)
        val error = player.error
        val positionMs = (player.currentTime * 1000).toLong()
        val durationMs = (player.duration * 1000).toLong()

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
                            player.togglePlay(isPlaying)
                            true
                        }
                        Key.DirectionLeft -> {
                            player.seekToMillis(positionMs - SeekStepMillis)
                            true
                        }
                        Key.DirectionRight -> {
                            player.seekToMillis(positionMs + SeekStepMillis)
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
            VideoPlayerSurface(
                playerState = player,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
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
                                player.togglePlay(isPlaying)
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
                    isPlaying = isPlaying,
                    isFullscreen = isFullscreen,
                    onTogglePlayPause = { player.togglePlay(isPlaying) },
                    onToggleFullscreen = { currentOnFullscreenChange(!currentIsFullscreen) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun VideoPlayerState.togglePlay(isPlaying: Boolean) {
    if (isPlaying) pause() else play()
}

/**
 * Seeks to an absolute position given in milliseconds, translating it to the player's
 * 0..1000 slider fraction. Clamped to the media duration (a no-op while the duration
 * is still unknown).
 */
private fun VideoPlayerState.seekToMillis(targetMs: Long) {
    val durationMs = duration * 1000.0
    if (durationMs <= 0.0) return
    val clampedMs = targetMs.toDouble().coerceIn(0.0, durationMs)
    seekTo((clampedMs / durationMs * 1000.0).toFloat().coerceIn(0f, 1000f))
}

package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

private val SpeedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

/**
 * Hand-built control bar overlaid on the video surface. ComposeMediaPlayer ships no
 * control UI, so this reproduces the controls the app needs: play/pause, seek bar with
 * time labels, playback-speed selection and fullscreen.
 *
 * Seeking goes through the player's own drag API ([VideoPlayerState.seekStart] /
 * [VideoPlayerState.seekFinished]), which suppresses position updates while the user
 * drags and keeps the thumb from fighting the playback clock.
 * [VideoPlayerState.sliderPos] is a 0..1000 fraction of the duration.
 *
 * Audio-track selection is dropped: ComposeMediaPlayer takes an [AudioMode] at
 * construction, it has no runtime audio-track switching.
 */
@Composable
internal fun PlayerControlBar(
    player: VideoPlayerState,
    isPlaying: Boolean,
    isFullscreen: Boolean,
    onTogglePlayPause: () -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings.player

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
            ),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm)) {
            Slider(
                value = player.sliderPos,
                onValueChange = { player.seekStart(it) },
                onValueChangeFinished = { player.seekFinished() },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth(),
                thumb = {
                    Box(
                        Modifier
                            .size(14.dp)
                            .shadow(1.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    )
                },
                track = { state ->
                    val fraction =
                        (state.value - state.valueRange.start) /
                            (state.valueRange.endInclusive - state.valueRange.start)
                    Canvas(Modifier.fillMaxWidth().height(3.dp)) {
                        val y = size.height / 2
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = size.height,
                            cap = StrokeCap.Square,
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, y),
                            end = Offset(size.width * fraction, y),
                            strokeWidth = size.height,
                            cap = StrokeCap.Square,
                        )
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) s.pause else s.play,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = "${player.positionText} / ${player.durationText}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))

                PlaybackSpeedMenu(player = player)

                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = if (isFullscreen) s.exitFullscreen else s.fullscreen,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackSpeedMenu(
    player: VideoPlayerState,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = "${formatSpeed(player.playbackSpeed)}x",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SpeedOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${formatSpeed(option)}x") },
                    onClick = {
                        player.playbackSpeed = option
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()

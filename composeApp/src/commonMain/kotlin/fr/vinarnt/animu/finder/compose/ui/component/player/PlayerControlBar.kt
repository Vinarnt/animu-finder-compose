package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.features.audioTracks
import org.openani.mediamp.metadata.AudioTrack

private val SpeedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

/**
 * Hand-built control bar overlaid on the video surface. MediaMP ships no control UI,
 * so this reproduces the controls the previous player provided: play/pause, seek bar
 * with time labels, playback-speed selection, audio-track selection and fullscreen.
 */
@Composable
internal fun PlayerControlBar(
    player: MediampPlayer,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    isFullscreen: Boolean,
    showAudioSelector: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings.player

    // While the user drags the seek bar, drive the displayed position from the drag
    // rather than the player so the thumb does not fight the playback clock.
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }
    val sliderPositionMs = (dragPositionMs ?: positionMs).coerceIn(0L, durationMs.coerceAtLeast(1L))

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
            ),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm)) {
            Slider(
                value = sliderPositionMs.toFloat(),
                onValueChange = { dragPositionMs = it.toLong() },
                onValueChangeFinished = {
                    dragPositionMs?.let(onSeek)
                    dragPositionMs = null
                },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
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
                    text = "${formatTime(sliderPositionMs)} / ${formatTime(durationMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))

                PlaybackSpeedMenu(player = player)

                if (showAudioSelector) {
                    AudioTrackMenu(player = player)
                }

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
    player: MediampPlayer,
    modifier: Modifier = Modifier,
) {
    val feature = player.features[PlaybackSpeed]
    var speed by remember { mutableStateOf(feature?.value ?: 1f) }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = "${formatSpeed(speed)}x",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SpeedOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${formatSpeed(option)}x") },
                    onClick = {
                        feature?.set(option)
                        speed = option
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AudioTrackMenu(
    player: MediampPlayer,
    modifier: Modifier = Modifier,
) {
    val trackGroup = player.audioTracks ?: return
    val candidates by trackGroup.candidates.collectAsStateWithLifecycle(emptyList())
    var expanded by remember { mutableStateOf(false) }

    // A single candidate offers nothing to switch to.
    if (candidates.size < 2) return

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Audiotrack,
                contentDescription = strings.player.audio,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            candidates.forEach { track ->
                DropdownMenuItem(
                    text = { Text(audioTrackLabel(track)) },
                    onClick = {
                        trackGroup.select(track)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun audioTrackLabel(track: AudioTrack): String =
    track.name ?: track.labels.firstOrNull()?.value ?: track.id

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

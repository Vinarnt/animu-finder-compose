package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import fr.vinarnt.animu.finder.compose.model.SubtitleCue
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import fr.vinarnt.animu.finder.compose.repository.SubtitleRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Renders soft subtitles (WebVTT) fetched from [SubtitleTrack.url] as a timed,
 * draggable overlay. These are external subtitle files rather than tracks embedded
 * in the media, so they are loaded and drawn here instead of by the player. Hard
 * subs are burned into the video and need no overlay.
 *
 * This composable renders inside the player's interaction overlay (the padded
 * tap-to-pause layer), so the subtitle text is a child of that overlay's gesture
 * node: taps on the text fall through to the pause/fullscreen gesture, and only
 * real drags are consumed by the text. The text is clamped so it stays over the
 * video surface, never overlapping the control bar, and its position
 * is stored as fractions of the player size (see [SubtitlePosition]) so it stays
 * put across resizes/fullscreen.
 */
@Composable
fun SubtitleOverlay(
    subtitles: List<SubtitleTrack>,
    currentTime: Float,
    modifier: Modifier = Modifier,
) {
    val repository = koinInject<SubtitleRepository>()
    val settings = koinInject<SettingManager>()
    val scope = rememberCoroutineScope()

    val softUrl = subtitles
        .firstOrNull { it.type == SubtitleType.Soft && !it.url.isNullOrBlank() }
        ?.url

    var cues by remember { mutableStateOf<List<SubtitleCue>>(emptyList()) }
    var position by remember { mutableStateOf(SubtitlePosition()) }

    LaunchedEffect(softUrl) {
        cues = if (softUrl != null) repository.load(softUrl) else emptyList()
    }

    LaunchedEffect(Unit) {
        position = settings.getSubtitlePosition()
    }

    val activeCue = remember(cues, currentTime) {
        val positionMs = (currentTime * 1000).toLong()
        cues.firstOrNull { positionMs in it.startMs until it.endMs }
    }

    val density = LocalDensity.current
    val controlBarHeightPx = with(density) { ControlBarHeight.toPx() }

    // This fills the interaction overlay (the padded video area). Position fractions
    // are relative to the FULL player, so the full height is recovered by adding back
    // the reserved control-bar strip.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val videoAreaW = constraints.maxWidth.toFloat()
        val videoAreaH = constraints.maxHeight.toFloat()
        val playerW = videoAreaW
        val playerH = videoAreaH + controlBarHeightPx

        if (activeCue != null) {
            var textSize by remember { mutableStateOf(IntSize.Zero) }
            val textW = textSize.width.toFloat()
            val textH = textSize.height.toFloat()
            val maxLeft = (playerW - textW).coerceAtLeast(0f)
            val maxTop = (playerH - textH).coerceAtLeast(0f)
            val left = (position.x * playerW - textW / 2f).coerceIn(0f, maxLeft)
            val top = (position.y * playerH - textH / 2f).coerceIn(0f, maxTop)

            val currentPosition by rememberUpdatedState(position)
            val currentPlayerW by rememberUpdatedState(playerW)
            val currentPlayerH by rememberUpdatedState(playerH)
            val currentTextW by rememberUpdatedState(textW)
            val currentTextH by rememberUpdatedState(textH)

            Text(
                text = activeCue.text,
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        offset = Offset(0f, 2f),
                        blurRadius = 8f,
                    ),
                ),
                modifier = Modifier
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .onSizeChanged { textSize = it }
                    .pointerInput(Unit) {
                        // Only actual drags are consumed (moves past touch slop); taps fall
                        // through to the parent interaction overlay. `positionChange` yields the
                        // pointer delta in the player's frame of reference, so the subtitle is
                        // moved relative to its last position by accumulating it inside the
                        // gesture. The composable state is only sampled once, at drag start.
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var dragged = false
                            var pending = Offset.Zero
                            var nodeLeft = 0f
                            var nodeTop = 0f
                            var nodeMaxLeft = 0f
                            var nodeMaxTop = 0f
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: break
                                if (!change.pressed) break
                                val delta = change.positionChange()
                                if (!dragged) {
                                    pending += delta
                                    if (pending.getDistance() <= viewConfiguration.touchSlop) {
                                        continue
                                    }
                                    dragged = true
                                    nodeMaxLeft = (currentPlayerW - currentTextW).coerceAtLeast(0f)
                                    nodeMaxTop = (currentPlayerH - currentTextH).coerceAtLeast(0f)
                                    nodeLeft = (currentPosition.x * currentPlayerW - currentTextW / 2f)
                                        .coerceIn(0f, nodeMaxLeft)
                                    nodeTop = (currentPosition.y * currentPlayerH - currentTextH / 2f)
                                        .coerceIn(0f, nodeMaxTop)
                                }
                                change.consume()
                                val move =
                                    if (pending != Offset.Zero) {
                                        pending.also { pending = Offset.Zero }
                                    } else {
                                        delta
                                    }
                                val newLeft = (nodeLeft + move.x).coerceIn(0f, nodeMaxLeft)
                                val newTop = (nodeTop + move.y).coerceIn(0f, nodeMaxTop)
                                nodeLeft = newLeft
                                nodeTop = newTop
                                // Store the text CENTER as a fraction of the player, which is how
                                // the position is interpreted when rendering.
                                position = SubtitlePosition(
                                    x = if (currentPlayerW > 0f)
                                        (newLeft + currentTextW / 2f) / currentPlayerW
                                    else currentPosition.x,
                                    y = if (currentPlayerH > 0f)
                                        (newTop + currentTextH / 2f) / currentPlayerH
                                    else currentPosition.y,
                                )
                            }
                            if (dragged) {
                                scope.launch { settings.setSubtitlePosition(position) }
                            }
                        }
                    },
            )
        }
    }
}
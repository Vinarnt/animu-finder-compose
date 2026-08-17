package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollIndicatorState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius as AppCornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shared overlay scrollbar implementation used on Desktop and WasmJS. Handles drawing, hover
 * highlighting, thumb dragging and track-press jumps.
 *
 * No-op elsewhere (Android/iOS rely on native scroll indicators).
 */
@Composable
internal fun Modifier.appVerticalScrollbarOverlayImpl(state: ScrollableState): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollMutex = remember { Mutex() }

    val trackColor by animateColorAsState(
        if (isHovered || isDragging) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        }
    )
    val indicatorColor by animateColorAsState(
        if (isHovered || isDragging) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        }
    )
    val minimalHeightPx = with(LocalDensity.current) { 32.dp.toPx() }
    val thicknessPx = with(LocalDensity.current) { Spacing.sm.toPx() }
    val cornerPx = with(LocalDensity.current) { AppCornerRadius.sm.toPx() }

    return this
        .hoverable(interactionSource)
        .pointerInput(state, thicknessPx, minimalHeightPx) {
            awaitEachGesture {
                val indicator = state.scrollIndicatorState ?: return@awaitEachGesture
                if (indicator.contentSize == Int.MAX_VALUE || indicator.viewportSize == Int.MAX_VALUE) {
                    return@awaitEachGesture
                }
                val content = indicator.contentSize
                val viewport = indicator.viewportSize
                if (content <= viewport) return@awaitEachGesture

                val trackLength = size.height.toFloat()
                val thumbLength = (size.height / content.toFloat() * trackLength)
                    .coerceAtLeast(minimalHeightPx)
                val maxThumbOffset = (trackLength - thumbLength).coerceAtLeast(0f)
                val scrollRange = (content - viewport).toFloat().coerceAtLeast(1f)
                val scrollScale = maxThumbOffset / scrollRange
                val gutterLeft = size.width - thicknessPx

                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                if (down.position.x < gutterLeft) return@awaitEachGesture
                down.consume()

                val currentScroll = indicator.scrollOffset.toFloat()
                val currentThumbOffset = (currentScroll / scrollRange * maxThumbOffset)
                    .coerceIn(0f, maxThumbOffset)
                val grabOffset =
                    if (down.position.y in currentThumbOffset..(currentThumbOffset + thumbLength)) {
                        down.position.y - currentThumbOffset
                    } else {
                        // Track press: center the thumb under the cursor.
                        val targetThumb = (down.position.y - thumbLength / 2f)
                            .coerceIn(0f, maxThumbOffset)
                        scrollToThumb(
                            targetThumb = targetThumb,
                            scrollScale = scrollScale,
                            scrollRange = scrollRange,
                            state = state,
                            mutex = scrollMutex,
                            scope = coroutineScope,
                        )
                        thumbLength / 2f
                    }

                isDragging = true
                try {
                    drag(down.id) { change ->
                        val targetThumb = (change.position.y - grabOffset)
                            .coerceIn(0f, maxThumbOffset)
                        scrollToThumb(
                            targetThumb = targetThumb,
                            scrollScale = scrollScale,
                            scrollRange = scrollRange,
                            state = state,
                            mutex = scrollMutex,
                            scope = coroutineScope,
                        )
                        change.consume()
                    }
                } finally {
                    isDragging = false
                }
            }
        }
        .drawWithContent {
            drawContent()
            state.scrollIndicatorState?.let { indicator ->
                drawAppScrollbar(
                    indicator = indicator,
                    trackColor = trackColor,
                    indicatorColor = indicatorColor,
                    minimalHeightPx = minimalHeightPx,
                    thicknessPx = thicknessPx,
                    cornerPx = cornerPx,
                )
            }
        }
}

private fun scrollToThumb(
    targetThumb: Float,
    scrollScale: Float,
    scrollRange: Float,
    state: ScrollableState,
    mutex: Mutex,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
        mutex.withLock {
            val targetScroll = (targetThumb / scrollScale).coerceIn(0f, scrollRange)
            val current = state.scrollIndicatorState?.scrollOffset?.toFloat() ?: return@withLock
            val delta = targetScroll - current
            if (delta != 0f) {
                state.scroll { scrollBy(delta) }
            }
        }
    }
}

private fun DrawScope.drawAppScrollbar(
    indicator: ScrollIndicatorState,
    trackColor: Color,
    indicatorColor: Color,
    minimalHeightPx: Float,
    thicknessPx: Float,
    cornerPx: Float,
) {
    if (indicator.contentSize == Int.MAX_VALUE || indicator.viewportSize == Int.MAX_VALUE) return

    val contentSize = indicator.contentSize
    val viewportSize = indicator.viewportSize
    if (contentSize <= viewportSize) return

    val viewportHeight = size.height
    val contentHeight = contentSize.toFloat()
    val trackLength = viewportHeight
    val indicatorLength = (viewportHeight / contentHeight * trackLength).coerceAtLeast(minimalHeightPx)
    val maxIndicatorOffset = (trackLength - indicatorLength).coerceAtLeast(0f)
    val scrollRange = (contentHeight - viewportHeight).coerceAtLeast(1f)
    val indicatorOffset = (indicator.scrollOffset.toFloat() / scrollRange * maxIndicatorOffset)
        .coerceIn(0f, maxIndicatorOffset)

    val trackX = size.width - thicknessPx
    val corner = CornerRadius(cornerPx, cornerPx)

    drawRoundRect(
        color = trackColor,
        topLeft = Offset(trackX, 0f),
        size = Size(thicknessPx, trackLength),
        cornerRadius = corner,
    )
    drawRoundRect(
        color = indicatorColor,
        topLeft = Offset(trackX, indicatorOffset),
        size = Size(thicknessPx, indicatorLength),
        cornerRadius = corner,
    )
}

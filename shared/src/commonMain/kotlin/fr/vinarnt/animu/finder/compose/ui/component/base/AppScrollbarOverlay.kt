package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollIndicatorState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius as AppCornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
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
        .drawWithContent {
            drawContent()
            state.scrollIndicatorState?.let { current ->
                drawAppScrollbar(
                    indicator = current,
                    trackColor = trackColor,
                    indicatorColor = indicatorColor,
                    minimalHeightPx = minimalHeightPx,
                    thicknessPx = thicknessPx,
                    cornerPx = cornerPx,
                )
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

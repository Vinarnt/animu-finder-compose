package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

/** Interactive hero slide dots: 28dp hit area, 16x5 pill; active pill is white + 1.4x scale. */
@Composable
internal fun HeroDots(
    index: Int,
    count: Int,
    onDotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(count) { dotIndex ->
            HeroDot(
                active = dotIndex == index,
                onClick = { onDotClick(dotIndex) },
            )
        }
    }
}

@Composable
private fun HeroDot(active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center
    ) {
        val pillScale by animateFloatAsState(
            targetValue = if (active) 1.4f else 1f,
            animationSpec = tween(200),
            label = "heroDotScale"
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(5.dp)
                .graphicsLayer { scaleX = pillScale }
                .background(
                    color = if (active) Color.White else Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(3.dp)
                )
        )
    }
}

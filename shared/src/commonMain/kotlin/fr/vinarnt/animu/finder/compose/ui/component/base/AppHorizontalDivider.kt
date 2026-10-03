package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's horizontal rule. Same shape as [HorizontalDivider], so it goes wherever that would.
 */
@Composable
fun AppHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline,
) {
    Canvas(modifier.fillMaxWidth().height(thickness)) {
        drawRect(
            color = color,
            topLeft = Offset.Zero,
            size = size,
        )
    }
}

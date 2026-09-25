package fr.vinarnt.animu.finder.compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlass
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlassBorder
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim

val BookmarkIcon: ImageVector = ImageVector.Builder(
    name = "BookmarkOutline",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(19f, 21f)
        lineTo(12f, 16f)
        lineTo(5f, 21f)
        verticalLineTo(5f)
        curveTo(5f, 3.9f, 5.9f, 3f, 7f, 3f)
        horizontalLineTo(17f)
        curveTo(18.1f, 3f, 19f, 3.9f, 19f, 5f)
        close()
    }
}.build()

@Composable
fun AddToMyListButton(
    inList: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val label = if (inList) strings.myList.addedLabel else strings.myList.addLabel
    val shape = RoundedCornerShape(CornerRadius.pill)

    Row(
        modifier = modifier
            .clip(shape)
            .background(if (inList) MaterialTheme.colorScheme.primary else heroGlass)
            .border(
                width = 1.dp,
                color = if (inList) Color.Transparent else heroGlassBorder,
                shape = shape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .scale(if (isPressed) 0.97f else 1f)
            .height(if (compact) 44.dp else 46.dp)
            .padding(horizontal = if (compact) 14.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = if (inList) Icons.Default.Check else BookmarkIcon,
            contentDescription = null,
            tint = if (inList) MaterialTheme.colorScheme.onPrimary else onScrim,
            modifier = Modifier.size(if (compact) Size.Icon.sm else Size.Icon.md),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (inList) MaterialTheme.colorScheme.onPrimary else onScrim,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
package fr.vinarnt.animu.finder.compose.ui.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import fr.vinarnt.animu.finder.compose.ui.LocalWindowControlsHost
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlass

/**
 * Icon button for the app's top bar.
 *
 * Where the app bar is the window's title bar, the button takes the size, flat fill and icon size
 * of the platform's window controls, so the two read as one row. Otherwise it is the app's own
 * button, with the glass pill when it sits over the hero.
 */
@Composable
fun AppBarIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    onGlass: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val controls = LocalWindowControlsHost.current

    if (controls.isHosting) {
        val interactionSource = remember { MutableInteractionSource() }
        val hovered by interactionSource.collectIsHoveredAsState()
        val contentColor = LocalContentColor.current

        Box(
            modifier = modifier
                .size(controls.buttonSize)
                .clip(RoundedCornerShape(CornerRadius.sm))
                .background(
                    if (hovered) contentColor.copy(alpha = HOVER_FILL_ALPHA) else Color.Transparent
                )
                .hoverable(interactionSource)
                .clickable(
                    interactionSource = null,
                    // The platform's controls answer a hover with artwork, not a ripple.
                    indication = null,
                    onClick = onClick,
                )
                .pointerHoverIcon(PointerIcon.Hand),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(controls.iconSize),
            )
        }
        return
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .then(
                if (onGlass) {
                    Modifier
                        .clip(CircleShape)
                        .background(heroGlass)
                } else {
                    Modifier
                }
            )
            .size(Size.TouchTarget.min)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (onGlass) Color.White else LocalContentColor.current,
        )
    }
}

private const val HOVER_FILL_ALPHA = 0.12f

package fr.vinarnt.animu.finder.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * The window controls the host wants drawn inside the app's top bar.
 *
 * On desktop the app bar is the window's title bar, so the shell hands it the platform's minimize,
 * maximize and close controls. The bar puts them on the edge they belong to, which keeps the title
 * and the app's actions clear of them.
 *
 * [leading] holds controls drawn on the left, which means the macOS traffic lights: those are native
 * buttons, so reserving their space is all that is needed. [trailing] holds the ones on the right.
 * Both are empty off desktop.
 *
 * [buttonSize] and [iconSize] are how that platform draws a control, so the bar's own buttons match
 * instead of looking like a different kind of widget beside them.
 */
data class WindowControlsHost(
    val leading: (@Composable () -> Unit)? = null,
    val trailing: (@Composable () -> Unit)? = null,
    val buttonSize: DpSize = DpSize(40.dp, 40.dp),
    val iconSize: Dp = 20.dp,
) {
    /** Whether this host puts the platform's controls in the app's bar at all. */
    val isHosting: Boolean get() = leading != null || trailing != null
}

val LocalWindowControlsHost = staticCompositionLocalOf { WindowControlsHost() }

/**
 * Whether the app bar is currently over artwork rather than a plain surface. The bar writes it and
 * the host reads it.
 *
 * It picks which artwork the window controls use. Over a hero the bar stays dark so the title and
 * controls read against any still, so the controls follow that rather than the app theme.
 */
val LocalAppBarOnImage = compositionLocalOf { mutableStateOf(false) }

/**
 * Color the host paints the window with, taken from the app theme.
 *
 * It only shows through the gaps Compose leaves: frames during a resize, and the moment before the
 * first screen composes. The chrome paints no background of its own, so the page shows through it.
 */
val LocalWindowBackgroundColor = compositionLocalOf { mutableStateOf<Color?>(null) }

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
 * The window controls the host wants drawn inside the app's own top bar.
 *
 * On desktop the app's top bar is the window's title bar, so the shell hands it the platform's
 * minimize / maximize / close controls and the bar lays them out at the edge they belong to. That
 * keeps the title and the app's own actions from ending up underneath them, without the app having
 * to know how wide the platform's controls are.
 *
 * [leading] is for the platforms that put the controls on the left (the macOS traffic lights, which
 * are native buttons: reserving their footprint is all that is needed); [trailing] is for the ones
 * that put them on the right. Both are empty off desktop, and on any platform whose chrome keeps
 * the controls to itself.
 *
 * [buttonSize] and [iconSize] describe how that platform draws a control, so the bar's own buttons
 * can be laid out to match instead of looking like a different family of widget next to them.
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
 * Whether the app's top bar is currently sitting on top of the page's artwork rather than on a
 * plain surface. Written by the bar, read by the host.
 *
 * It decides which artwork the platform's window controls use: over a hero the bar is kept dark so
 * the title and the controls stay readable on any still, and the controls have to follow that
 * rather than the app's own light or dark theme.
 */
val LocalAppBarOnImage = compositionLocalOf { mutableStateOf(false) }

/**
 * Color the host should paint the window with, read from the app theme.
 *
 * Only the gaps Compose leaves matter: the frames drawn during a live resize, and the moment
 * between the window appearing and the first screen composing. The window chrome paints no
 * background of its own, so the page behind it shows through, which leaves this as the only color
 * behind those gaps.
 */
val LocalWindowBackgroundColor = compositionLocalOf { mutableStateOf<Color?>(null) }

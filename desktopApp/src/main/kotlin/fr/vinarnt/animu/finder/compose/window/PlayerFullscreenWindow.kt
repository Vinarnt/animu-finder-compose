package fr.vinarnt.animu.finder.compose.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.delay

/**
 * Size to create the window with, taken from the screen.
 *
 * The creation size becomes the window's restored size, which is what the compositor falls back to
 * when Nucleus un-maximizes before applying a placement. Oversizing it to the screen keeps that
 * step off-screen instead of shrinking the window to a small default on every fullscreen toggle.
 */
@Composable
internal fun rememberInitialWindowSize(): DpSize =
    remember {
        runCatching {
            val bounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .defaultScreenDevice.defaultConfiguration.bounds
            DpSize(bounds.width.dp, bounds.height.dp)
        }.getOrElse { DpSize(1280.dp, 800.dp) }
    }

/**
 * Puts the window into fullscreen while [immersive] is true, and returns it to the placement it
 * had before once that goes false.
 *
 * Nucleus needs two workarounds for the transition to be clean:
 *
 *  1. Applying a placement undoes the old one first (`setMaximized(false)`, then
 *     `setFullscreen(true)`), which restores the window to its requested size. That size is kept
 *     equal to the real maximized size, so the restored window is the size it already had.
 *  2. Leaving fullscreen leaves the window at that restored size while still reporting
 *     `Maximized`, so no maximize is re-issued. The placement goes through `Floating` once to
 *     force one.
 *
 * [rememberInitialWindowSize] covers the other half of the first point: the size the window was
 * created with is the one the compositor restores.
 */
@Composable
internal fun PlayerFullscreenWindowEffect(
    windowState: WindowState,
    immersive: State<Boolean>,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    // Largest size seen while maximized, which is the size the window should have when it is not
    // fullscreen.
    var maximizedSize by remember { mutableStateOf<DpSize?>(null) }
    var placementBeforeFullscreen by remember { mutableStateOf(windowState.placement) }
    var ensureMaximized by remember { mutableStateOf(false) }

    fun containerDp(): DpSize =
        with(density) {
            DpSize(windowInfo.containerSize.width.toDp(), windowInfo.containerSize.height.toDp())
        }

    // Keep the window's requested size equal to its maximized size, so the un-maximize step of a
    // placement transition restores the geometry it already had.
    LaunchedEffect(Unit) {
        snapshotFlow { windowInfo.containerSize to windowState.placement }
            .collect { (px, placement) ->
                if (px.width <= 0 || px.height <= 0) return@collect
                if (placement != WindowPlacement.Maximized) return@collect
                val dp = with(density) { DpSize(px.width.toDp(), px.height.toDp()) }
                val known = maximizedSize
                if (known == null || dp.width > known.width || dp.height > known.height) {
                    maximizedSize = dp
                    if (windowState.size != dp) windowState.size = dp
                }
            }
    }

    LaunchedEffect(immersive.value) {
        if (immersive.value) {
            placementBeforeFullscreen =
                windowState.placement.takeIf { it != WindowPlacement.Fullscreen }
                    ?: WindowPlacement.Maximized
            ensureMaximized = false
            windowState.placement = WindowPlacement.Fullscreen
        } else {
            if (windowState.placement == WindowPlacement.Fullscreen) {
                windowState.placement = placementBeforeFullscreen
            }
            ensureMaximized = placementBeforeFullscreen == WindowPlacement.Maximized
            if (ensureMaximized) {
                delay(450)
                ensureMaximized = false
            }
        }
    }

    // Force a maximize when the window came back smaller than the maximized size. Nucleus reports
    // `Maximized` without re-issuing the request, so the placement goes through `Floating` once
    // (same geometry) to make a maximize actually land.
    LaunchedEffect(windowState.placement, ensureMaximized, maximizedSize) {
        if (!ensureMaximized) return@LaunchedEffect
        val target = maximizedSize ?: return@LaunchedEffect
        if (containerDp().width >= target.width) return@LaunchedEffect
        windowState.placement = WindowPlacement.Floating
        delay(60)
        windowState.placement = WindowPlacement.Maximized
    }
}

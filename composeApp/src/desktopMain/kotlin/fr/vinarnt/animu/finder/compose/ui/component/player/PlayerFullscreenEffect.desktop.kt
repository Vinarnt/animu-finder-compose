package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.DpSize
import fr.vinarnt.animu.finder.compose.LocalAppWindowState
import kotlinx.coroutines.delay

@Composable
actual fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
) {
    val windowState = LocalAppWindowState.current

    var previousPlacement by remember { mutableStateOf<WindowPlacement?>(null) }
    var previousSize by remember { mutableStateOf<DpSize?>(null) }
    var previousPosition by remember { mutableStateOf<WindowPosition?>(null) }

    LaunchedEffect(isFullscreen) {
        val ws = windowState ?: return@LaunchedEffect
        if (isFullscreen) {
            if (ws.placement != WindowPlacement.Fullscreen) {
                previousPlacement = ws.placement
                previousSize = ws.size
                previousPosition = ws.position
            }
            ws.placement = WindowPlacement.Fullscreen
        } else {
            restoreWindowState(ws, previousPlacement, previousSize, previousPosition)
        }
    }

    DisposableEffect(isFullscreen) {
        onDispose {
            if (isFullscreen) {
                // Fallback for when the effect leaves composition while fullscreen
                // (e.g. navigating away): at least restore the placement.
                windowState?.placement = previousPlacement ?: WindowPlacement.Maximized
            }
        }
    }

    LaunchedEffect(Unit) {
        val ws = windowState ?: return@LaunchedEffect
        fun log(label: String) {
            java.io.File("/tmp/fs_diag.txt").appendText("$label placement=${ws.placement} size=${ws.size} pos=${ws.position}\n")
        }
        log("start")
        delay(2000)
        val ps = ws.size
        val ppos = ws.position
        ws.placement = WindowPlacement.Fullscreen
        delay(3000)
        log("fullscreen")
        ws.placement = WindowPlacement.Maximized
        ws.size = ps
        ws.position = ppos
        log("after-restore")
        delay(1000)
        log("settled-1s")
        delay(1000)
        log("settled-2s")
    }
}

private suspend fun restoreWindowState(
    windowState: WindowState,
    previousPlacement: WindowPlacement?,
    previousSize: DpSize?,
    previousPosition: WindowPosition?,
) {
    when (previousPlacement) {
        // Restore the exact bounds for a Floating window, since some window
        // managers don't re-derive the geometry from the placement alone after
        // exiting Fullscreen.
        WindowPlacement.Floating -> {
            windowState.placement = WindowPlacement.Floating
            if (previousSize != null && previousPosition != null) {
                windowState.size = previousSize
                windowState.position = previousPosition
            }
        }
        // Maximized / Fullscreen (and the null fallback): pass through Floating
        // (keeping the current geometry) before Maximizing, because some window
        // managers won't exit Fullscreen straight to Maximized. Crucially we do
        // NOT set the stale size/position captured before entering fullscreen —
        // that's what caused the window to shrink then re-expand.
        WindowPlacement.Maximized,
        WindowPlacement.Fullscreen,
        null,
        -> {
            windowState.placement = WindowPlacement.Floating
            delay(150)
            windowState.placement = WindowPlacement.Maximized
        }
    }
}

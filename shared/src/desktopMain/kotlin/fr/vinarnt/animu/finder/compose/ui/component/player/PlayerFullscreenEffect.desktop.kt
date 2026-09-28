package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.WindowPlacement
import fr.vinarnt.animu.finder.compose.LocalAppWindowState

@Composable
actual fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
) {
    val windowState = LocalAppWindowState.current

    LaunchedEffect(windowState, isFullscreen) {
        if (windowState == null) return@LaunchedEffect
        windowState.placement =
            if (isFullscreen) WindowPlacement.Fullscreen else WindowPlacement.Maximized
    }

    // Leaving the player restores the normal window.
    DisposableEffect(windowState) {
        onDispose { windowState?.placement = WindowPlacement.Maximized }
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable

@Composable
actual fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
) {
    // No-op on Android: the player fills the window via the fullscreen overlay layout.
}

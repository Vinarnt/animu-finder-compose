 package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import fr.vinarnt.animu.finder.compose.LocalAppWindow

@Composable
actual fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
) {
    val window = LocalAppWindow.current

    DisposableEffect(isFullscreen) {
        if (window == null) {
            return@DisposableEffect onDispose { }
        }

        val device = window.graphicsConfiguration.device

        if (isFullscreen) {
            if (device.isFullScreenSupported) {
                device.fullScreenWindow = window
            }
        } else if (device.fullScreenWindow === window) {
            device.fullScreenWindow = null
        }

        onDispose {
            if (isFullscreen && device.fullScreenWindow === window) {
                device.fullScreenWindow = null
            }
        }
    }
}
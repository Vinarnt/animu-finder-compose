package fr.vinarnt.animu.finder.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fr.vinarnt.animu.finder.compose.logger.initKermitLogging
import java.awt.Window as AwtWindow

// The real desktop AWT window, exposed to the player's fullscreen handling.
val LocalAppWindow = compositionLocalOf<AwtWindow?> { null }

fun main() {
    initKermitLogging()
    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp, placement = WindowPlacement.Maximized)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Animu Finder",
            state = windowState,
        ) {
            CompositionLocalProvider(LocalAppWindow provides window) {
                App()
            }
        }
    }
}
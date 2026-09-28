package fr.vinarnt.animu.finder.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.TitleBar
import fr.vinarnt.animu.finder.compose.logger.initKermitLogging

/**
 * Desktop entry point.
 *
 * Uses the Nucleus **Tao** backend because the in-app WebView (Cloudflare clearance
 * capture) embeds the system web engine as a native surface, which the plain Compose
 * Desktop window cannot host.
 */
fun main() {
    initKermitLogging()
    nucleusApplication(backend = NucleusBackend.Tao) {
        val windowState = rememberWindowState(
            size = DpSize(1280.dp, 800.dp),
            placement = WindowPlacement.Maximized,
        )
        DecoratedWindow(
            onCloseRequest = { exitApplication() },
            state = windowState,
            title = "Animu Finder",
        ) {
            TitleBar { }
            CompositionLocalProvider(LocalAppWindowState provides windowState) {
                App()
            }
        }
    }
}

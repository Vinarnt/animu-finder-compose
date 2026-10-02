package fr.vinarnt.animu.finder.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.core.runtime.LinuxDesktopEnvironment
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.window.*
import dev.nucleusframework.window.styling.LocalTitleBarStyle
import dev.nucleusframework.window.utils.linux.rememberLinuxButtonLayout
import fr.vinarnt.animu.finder.compose.logger.initKermitLogging
import fr.vinarnt.animu.finder.compose.ui.LocalAppBarOnImage
import fr.vinarnt.animu.finder.compose.ui.LocalWindowBackgroundColor
import fr.vinarnt.animu.finder.compose.ui.LocalWindowControlsHost
import fr.vinarnt.animu.finder.compose.ui.WindowControlsHost
import fr.vinarnt.animu.finder.compose.ui.component.player.LocalPlayerImmersive
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.window.PlayerFullscreenWindowEffect
import fr.vinarnt.animu.finder.compose.window.rememberInitialWindowSize

/**
 * Desktop entry point.
 *
 * Runs on the Nucleus **Tao** backend: the in-app WebView used for Cloudflare clearance capture
 * embeds the system web engine as a native surface, which a plain Compose Desktop window cannot
 * host.
 *
 * The window's title bar is the app's own top bar. This file builds the shell around it: the drag
 * area over that bar, the window controls the bar draws, and the window size and placement.
 * Player fullscreen is [PlayerFullscreenWindowEffect]; the window size it relies on is
 * [rememberInitialWindowSize].
 */
fun main() {
    initKermitLogging()
    nucleusApplication(backend = NucleusBackend.Tao) {
        val windowState = rememberWindowState(
            size = rememberInitialWindowSize(),
            placement = WindowPlacement.Maximized,
        )
        DecoratedWindow(
            onCloseRequest = { exitApplication() },
            state = windowState,
            title = "Animu Finder",
        ) {
            val immersive = remember { mutableStateOf(false) }
            val windowBackground = remember { mutableStateOf<Color?>(null) }
            PlayerFullscreenWindowEffect(windowState, immersive)

            // Nucleus owns where the controls go: on Linux it follows the desktop's own button
            // layout, and macOS keeps its native traffic lights on the left. The app's top bar
            // lays them out on that side, so its title and its actions stay clear of them.
            //
            // The sizes mirror how Nucleus draws them, so the bar's own buttons can be laid out to
            // match instead of reading as a different family of widget.
            val linuxLayout =
                if (Platform.Current == Platform.Linux) rememberLinuxButtonLayout() else null
            val controlsOnLeft =
                Platform.Current == Platform.MacOS || linuxLayout?.controlsOnRight == false
            val kde = LinuxDesktopEnvironment.Current == LinuxDesktopEnvironment.KDE
            val controlSize = when {
                Platform.Current != Platform.Linux -> DpSize(46.dp, Size.AppBar.height)
                kde -> DpSize(28.dp, 28.dp)
                else -> DpSize(40.dp, 40.dp)
            }
            val controlIconSize = when {
                Platform.Current != Platform.Linux -> 16.dp
                kde -> 20.dp
                else -> 24.dp
            }
            val windowControls = remember(controlsOnLeft, controlSize, controlIconSize) {
                val controls: @Composable () -> Unit = { WindowControls() }
                WindowControlsHost(
                    leading = controls.takeIf { controlsOnLeft },
                    trailing = controls.takeIf { !controlsOnLeft },
                    buttonSize = controlSize,
                    iconSize = controlIconSize,
                )
            }

            val appBarOnImage = remember { mutableStateOf(false) }
            val barOnImage = appBarOnImage.value
            val chromeIsDark = barOnImage || (windowBackground.value?.luminance() ?: 0f) < 0.5f
            val chromeStyle = remember(chromeIsDark) {
                if (chromeIsDark) {
                    DecoratedWindowDefaults.darkTitleBarStyle()
                } else {
                    DecoratedWindowDefaults.lightTitleBarStyle()
                }
            }

            val titleBarSlot: (@Composable () -> Unit)? =
                if (immersive.value) {
                    null
                } else {
                    ({
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Size.AppBar.height)
                                .windowDragArea(),
                        )
                    })
                }

            windowBackground.value?.let { WindowBackground(it) }

            CompositionLocalProvider(
                LocalTitleBarStyle provides chromeStyle,
                LocalIsDarkTheme provides chromeIsDark,
                LocalAppBarOnImage provides appBarOnImage,
            ) {
                WindowScaffold(
                    titleBar = titleBarSlot,
                    titleBarPlacement = TitleBarPlacement.Overlay(
                        autoHideInFullscreen = true,
                        passThroughToContent = true,
                    ),
                ) {
                    CompositionLocalProvider(
                        LocalPlayerImmersive provides immersive,
                        LocalWindowControlsHost provides windowControls,
                        LocalWindowBackgroundColor provides windowBackground,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            App()
                        }
                    }
                }
            }
        }
    }
}

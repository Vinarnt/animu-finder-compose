package fr.vinarnt.animu.finder.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.WindowState

/**
 * The desktop window state, exposed to the player's fullscreen handling.
 *
 * Desktop runs on a Nucleus **Tao** window, which has no AWT window to toggle, so
 * fullscreen goes through [WindowState.placement] (`WindowPlacement.Fullscreen`); the
 * window's `DecoratedWindowState` tracks that.
 */
val LocalAppWindowState = compositionLocalOf<WindowState?> { null }

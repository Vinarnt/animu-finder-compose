package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the player is presented fullscreen.
 *
 * Provided above the screens by the window host, because the desktop chrome has to react to it:
 * the title bar draws above the content, and while the player is fullscreen it must stay out of
 * the way, including the frames after the OS window has left fullscreen but the page has not been
 * restored. Driving the bar from the window's own state alone slid it back in over the
 * still-fullscreen video.
 *
 * Defaults to a local state where the window host provides none, so writing to it is always safe.
 */
val LocalPlayerImmersive = staticCompositionLocalOf { mutableStateOf(false) }

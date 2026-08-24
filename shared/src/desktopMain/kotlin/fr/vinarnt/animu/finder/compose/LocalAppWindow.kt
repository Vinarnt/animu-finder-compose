package fr.vinarnt.animu.finder.compose

import androidx.compose.runtime.compositionLocalOf
import java.awt.Window as AwtWindow

// The real desktop AWT window, exposed to the player's fullscreen handling.
val LocalAppWindow = compositionLocalOf<AwtWindow?> { null }

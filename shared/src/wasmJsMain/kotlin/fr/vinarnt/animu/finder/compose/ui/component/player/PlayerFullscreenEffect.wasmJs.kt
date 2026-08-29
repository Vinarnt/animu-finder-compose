package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable

/**
 * No-op on wasmJs: the web player (`VideoPlayer.wasmJs`) drives the browser's native
 * Fullscreen API on its own container element directly (fullscreening the container,
 * not the document, so the browser sizes it to the actual screen). The Compose
 * fullscreen state is not used on this target.
 */
@Composable
actual fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
) {
}
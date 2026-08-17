package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable

@Composable
expect fun PlayerFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit,
)

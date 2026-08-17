package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
internal actual fun appVerticalScrollbarInset(): Dp = Spacing.sm + Spacing.xs

@Composable
internal actual fun Modifier.appVerticalScrollbarOverlay(state: ScrollableState): Modifier =
    appVerticalScrollbarOverlayImpl(state)

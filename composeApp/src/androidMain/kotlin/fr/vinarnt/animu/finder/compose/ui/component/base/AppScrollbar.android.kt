package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal actual fun Modifier.appVerticalScrollbarOverlay(state: ScrollableState): Modifier = this

@Composable
internal actual fun appVerticalScrollbarInset(): Dp = 0.dp

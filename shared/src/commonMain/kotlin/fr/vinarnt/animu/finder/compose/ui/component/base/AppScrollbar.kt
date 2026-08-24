package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.padding

/**
 * Adds vertical scrolling plus an overlay scrollbar to a non-lazy scrollable (e.g. [androidx.compose.foundation.layout.Column]).
 *
 * Handles scrolling itself — no separate [androidx.compose.foundation.verticalScroll] call required. On platforms with
 * native scroll indicators (Android, iOS, WasmJS) the scrollbar is omitted; the modifier only applies [verticalScroll].
 */
@Composable
fun Modifier.appVerticalScrollbar(state: ScrollState): Modifier =
    then(appVerticalScrollbarOverlay(state))
        .verticalScroll(state)
        .padding(end = appVerticalScrollbarInset())

/**
 * Adds an overlay scrollbar to a lazy scrollable ([LazyColumn] / [LazyRow]) without changing its layout.
 *
 * Scrolling is handled by the lazy composable itself, so the modifier only adds the scrollbar overlay on platforms
 * where it applies, and is a no-op elsewhere.
 */
@Composable
fun Modifier.appVerticalScrollbar(state: LazyListState): Modifier =
    appVerticalScrollbarOverlay(state).padding(end = appVerticalScrollbarInset())

/**
 * Adds an overlay scrollbar to a lazy grid ([LazyVerticalGrid]) without changing its layout.
 */
@Composable
fun Modifier.appVerticalScrollbar(state: LazyGridState): Modifier =
    appVerticalScrollbarOverlay(state).padding(end = appVerticalScrollbarInset())

/**
 * Draws the themed overlay scrollbar for the given scroll state. No-op where native scroll indicators are preferred.
 * Must be applied after (outside of) the scroll modifier so [androidx.compose.ui.draw.drawWithContent] measures the viewport.
 */
@Composable
internal expect fun Modifier.appVerticalScrollbarOverlay(state: ScrollableState): Modifier

/**
 * Horizontal inset reserved at the end of the scrollable content to keep the overlay scrollbar from covering content.
 * [Dp.Zero] where the overlay scrollbar is not drawn.
 */
@Composable
internal expect fun appVerticalScrollbarInset(): Dp

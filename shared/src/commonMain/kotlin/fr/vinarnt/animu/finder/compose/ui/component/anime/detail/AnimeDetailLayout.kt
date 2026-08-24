package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun AnimeDetailLayout(
    anime: GetAnimeById200ResponseData,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val isExpandedState = remember { mutableStateOf(true) }
    var isExpanded by isExpandedState

    val nestedScrollConnection = remember(lazyListState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f && isExpandedState.value) {
                    isExpandedState.value = false
                } else if (available.y > 1f && !isExpandedState.value) {
                    val isAtTop =
                        (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
                    if (isAtTop) {
                        isExpandedState.value = true
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 1f && !isExpandedState.value) {
                    isExpandedState.value = true
                }
                return Offset.Zero
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .pointerInput(isExpandedState) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            if (scrollDelta > 0f) isExpandedState.value = false
                            else if (scrollDelta < 0f) isExpandedState.value = true
                        }
                    }
                }
            }
    ) {
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding + Spacing.sm, vertical = Spacing.sm)
            ) {
                AnimeDetailLayoutStaticHeader(anime = anime, isExpanded = isExpanded)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (anime.synopsis != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = sidePadding + Spacing.sm, vertical = Spacing.sm)
                    ) {
                        AnimeDetailHeaderExpandContent(anime.synopsis!!)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimeDetailLayoutContent(modifier, lazyListState)
            }
        }
    }
}

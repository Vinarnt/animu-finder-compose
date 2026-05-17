package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.Anime

@Composable
fun AnimeDetailLayout(
    anime: Anime,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val isExpandedState = remember { mutableStateOf(true) }
    var isExpanded by isExpandedState

    val nestedScrollConnection = remember(lazyListState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Collapse when scrolling down
                if (available.y < -1f && isExpandedState.value) {
                    isExpandedState.value = false
                }

                // Expand when scrolling up and content is at the top
                else if (available.y > 1f && !isExpandedState.value) {
                    val isAtTop =
                        (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
                    if (isAtTop) {
                        isExpandedState.value = true
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Expand when scrolling up and content has reached the top
                if (available.y > 1f && !isExpandedState.value) {
                    isExpandedState.value = true
                }
                return Offset.Zero
            }
        }
    }

    Column(
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm)
                .shadow(elevation = Elevation.lg, shape = RoundedCornerShape(CornerRadius.md)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(CornerRadius.md),
            tonalElevation = Elevation.sm
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimeDetailLayoutStaticHeader(anime, isExpanded)

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    if (anime.synopsis != null) {
                        AnimeDetailHeaderExpandContent(anime.synopsis!!)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimeDetailLayoutContent(modifier, lazyListState)
        }
    }
}

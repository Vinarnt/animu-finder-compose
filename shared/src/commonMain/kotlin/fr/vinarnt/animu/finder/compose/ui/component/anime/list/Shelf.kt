package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * A home-page shelf.
 *
 * @param seeAll whether to show the "See all ›" action (navigates to the browse section).
 * @param onSeeAll invoked when "See all ›" is tapped. Ignored when [seeAll] is false.
 */
@Composable
fun Shelf(
    title: String,
    modifier: Modifier = Modifier,
    seeAll: Boolean = true,
    onSeeAll: (() -> Unit)? = null,
    rowContent: LazyListScope.() -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        ShelfHeader(
            title = title,
            seeAll = seeAll,
            onSeeAll = onSeeAll,
            listState = listState,
            onScroll = { forward ->
                scope.launch {
                    val viewportWidth =
                        listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                    listState.animateScrollBy((if (forward) viewportWidth else -viewportWidth) * 0.8f)
                }
            },
        )
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            rowContent()
        }
    }
}

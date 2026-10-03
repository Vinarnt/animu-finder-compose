package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.WindowSizeClass
import fr.vinarnt.animu.finder.compose.ui.theme.windowSizeClass
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun AnimeDetailLayout(
    anime: GetAnimeById200ResponseData,
    modifier: Modifier = Modifier,
    onScrolledChange: (Boolean) -> Unit = {},
    heroContent: @Composable () -> Unit = {},
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }.collect { scrolled -> onScrolledChange(scrolled) }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val compact = windowSizeClass().isAtMost(WindowSizeClass.Medium)
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        val gutter = sidePadding + if (compact) 16.dp else 24.dp
        Column(modifier = Modifier.fillMaxSize()) {
            heroContent()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = gutter)
            ) {
                AnimeDetailHeaderExpandContent(anime)
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimeDetailLayoutContent(lazyListState = lazyListState)
            }
        }
    }
}
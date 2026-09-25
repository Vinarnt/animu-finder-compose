package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.ContinueWatchingItem
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/** A home shelf to render, with its title, items, and "See all" action. */
internal data class HomeShelfSpec(
    val title: String,
    val items: List<GetAnime200ResponseDataInner>,
    val onSeeAll: () -> Unit,
)

@Composable
internal fun HomeHeader(
    loadedItems: List<GetAnime200ResponseDataInner>,
    resumeItems: List<ContinueWatchingItem>,
    myList: List<Int>,
    shelves: List<HomeShelfSpec>,
    onPlay: (GetAnime200ResponseDataInner) -> Unit,
    onToggleMyList: (GetAnime200ResponseDataInner) -> Unit,
    onOpenBrowse: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unknownFallback = strings.common.unknown

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        if (loadedItems.isNotEmpty()) {
            HomeHeroCarousel(
                items = loadedItems,
                onPlay = onPlay,
                myList = myList,
                onToggleMyList = onToggleMyList,
            )
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (loadedItems.isNotEmpty()) {
                BrowseAllPanel(onClick = onOpenBrowse)
            }

            HomeSearchBar(onSearch = onSearch)

            ContinueWatchingRow(items = resumeItems)

            shelves.forEach { shelf ->
                if (shelf.items.isNotEmpty()) {
                    HomeShelf(
                        title = shelf.title,
                        items = shelf.items,
                        unknownFallback = unknownFallback,
                        onSeeAll = shelf.onSeeAll,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(Spacing.md))
    }
}

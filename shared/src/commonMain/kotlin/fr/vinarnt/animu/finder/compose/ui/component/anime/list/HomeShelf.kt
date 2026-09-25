package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.runtime.Composable
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/** A home shelf: a titled [Shelf] rendering [items] as anime cards. */
@Composable
internal fun HomeShelf(
    title: String,
    items: List<GetAnime200ResponseDataInner>,
    unknownFallback: String,
    onSeeAll: () -> Unit,
) {
    Shelf(
        title = title,
        onSeeAll = onSeeAll,
    ) {
        animeShelfCards(items = items.take(10), unknownFallback = unknownFallback)
    }
}

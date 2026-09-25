package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/** Populates a home shelf row with a horizontal list of [AnimeCard]s. */
internal fun LazyListScope.animeShelfCards(
    items: List<GetAnime200ResponseDataInner>,
    unknownFallback: String,
) {
    items(
        count = items.size,
        key = { index -> "home:shelf:$index" }
    ) { index ->
        val anime = items[index]
        AnimeCard(
            title = anime.titles?.firstOrNull()?.title ?: unknownFallback,
            malId = anime.malId ?: 0,
            thumbnailUrl = anime.images?.jpg?.imageUrl
                ?: anime.images?.webp?.imageUrl ?: "",
            score = anime.score,
            subtitle = buildList {
                anime.type?.let { add(it) }
                anime.year?.let { add(it.toString()) }
            }.joinToString(" · ").ifEmpty { null },
            modifier = Modifier.width(Size.Card.shelfWidth),
        )
    }
}

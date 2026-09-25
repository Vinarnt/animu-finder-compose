package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.MultiSelectChip
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.SearchBar
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters
import fr.vinarnt.jikan4k.apis.AnimeApi
import kotlin.math.roundToInt

/** Browse search field plus the type / status / rating / genre / score / sort filter chips. */
@Composable
fun AnimeListFilters(
    filters: AnimeSearchFilters,
    onQueryChange: (String) -> Unit,
    onTypeChange: (AnimeApi.TypeGetAnime?) -> Unit,
    onStatusChange: (AnimeApi.StatusGetAnime?) -> Unit,
    onRatingChange: (AnimeApi.RatingGetAnime?) -> Unit,
    onGenresChange: (Set<AnimeGenre>) -> Unit,
    onScoreRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onScoreRangeCommit: () -> Unit,
    onSortChange: (AnimeApi.OrderByGetAnime, AnimeApi.SortGetAnime) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        SearchBar(
            query = filters.queryText,
            onValueChange = onQueryChange
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SingleSelectChip(
                text = "${strings.animeList.typeLabel}: ${strings.animeList.typeLabels[filters.type] ?: strings.animeList.all}",
                selected = filters.type != null,
                options = listOf(null) + AnimeApi.TypeGetAnime.entries,
                displayOption = { strings.animeList.typeLabels[it] ?: strings.animeList.all },
                onValueChange = onTypeChange
            )
            SingleSelectChip(
                text = "${strings.animeList.statusLabel}: ${strings.animeList.statusLabels[filters.status] ?: strings.animeList.all}",
                selected = filters.status != null,
                options = listOf(null) + AnimeApi.StatusGetAnime.entries,
                displayOption = { strings.animeList.statusLabels[it] ?: strings.animeList.all },
                onValueChange = onStatusChange
            )
            SingleSelectChip(
                text = "${strings.animeList.ratingLabel}: ${strings.animeList.ratingLabels[filters.rating] ?: strings.animeList.all}",
                selected = filters.rating != null,
                options = listOf(null) + AnimeApi.RatingGetAnime.entries,
                displayOption = { strings.animeList.ratingLabels[it] ?: strings.animeList.all },
                onValueChange = onRatingChange
            )
            val genreNames = strings.animeList.genreNames
            val genreSummary = when {
                filters.genres.isEmpty() -> strings.animeList.all
                filters.genres.size == 1 -> genreNames[filters.genres.first()] ?: filters.genres.first().name
                else -> strings.animeList.genreCount(filters.genres.size)
            }
            MultiSelectChip(
                text = "${strings.animeList.genreLabel}: $genreSummary",
                selected = filters.genres.isNotEmpty(),
                options = AnimeGenre.entries.toList(),
                selectedItems = filters.genres,
                displayOption = { genreNames[it] ?: it.name },
                filter = { genre, query -> (genreNames[genre] ?: genre.name).contains(query, ignoreCase = true) },
                onToggle = { genre ->
                    onGenresChange(
                        if (genre in filters.genres) filters.genres - genre else filters.genres + genre
                    )
                }
            )
            ScoreFilterChip(
                value = filters.scoreRange,
                onValueChange = onScoreRangeChange,
                onValueChangeFinished = onScoreRangeCommit,
                text = "${strings.common.score}: ${filters.scoreRange.start.roundToInt()} – ${filters.scoreRange.endInclusive.roundToInt()}"
            )
            SortChip(
                orderBy = filters.orderBy,
                sort = filters.sort,
                onSortChange = onSortChange,
            )
        }
    }
}

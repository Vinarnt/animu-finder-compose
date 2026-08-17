package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.component.base.input.RangeSlider
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.MultiSelectDropdown
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.Select
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.SearchBar
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.jikan4k.models.AnimeTypes
import kotlin.math.roundToInt

@Composable
fun AnimeListFilters(
    filters: AnimeSearchFilters,
    onQueryChange: (String) -> Unit,
    onTypeChange: (AnimeTypes?) -> Unit,
    onStatusChange: (AnimeSearchQueryStatus?) -> Unit,
    onRatingChange: (AnimeSearchQueryRating?) -> Unit,
    onGenresChange: (Set<AnimeGenre>) -> Unit,
    onScoreRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onScoreRangeCommit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = Spacing.sm),
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
            Select(
                selectedValue = filters.type,
                options = listOf(null) + AnimeTypes.entries,
                label = strings.animeList.typeLabel,
                onValueChangedEvent = onTypeChange,
                displayOption = { strings.animeList.typeLabels[it] ?: strings.animeList.all }
            )
            Select(
                selectedValue = filters.status,
                options = listOf(null) + AnimeSearchQueryStatus.entries,
                label = strings.animeList.statusLabel,
                onValueChangedEvent = onStatusChange,
                displayOption = { strings.animeList.statusLabels[it] ?: strings.animeList.all }
            )
            Select(
                selectedValue = filters.rating,
                options = listOf(null) + AnimeSearchQueryRating.entries,
                label = strings.animeList.ratingLabel,
                onValueChangedEvent = onRatingChange,
                displayOption = { strings.animeList.ratingLabels[it] ?: strings.animeList.all }
            )
            val genreNames = strings.animeList.genreNames
            MultiSelectDropdown(
                selectedItems = filters.genres,
                options = AnimeGenre.entries.toList(),
                label = strings.animeList.genreLabel,
                onSelectionChange = onGenresChange,
                displayOption = { strings.animeList.genreNames[it] ?: it.name },
                displaySummary = { selected ->
                    when {
                        selected.isEmpty() -> strings.animeList.all
                        selected.size == 1 -> strings.animeList.genreNames[selected.first()] ?: selected.first().name
                        else -> strings.animeList.genreCount(selected.size)
                    }
                },
                filter = { genre, query -> (genreNames[genre] ?: genre.name).contains(query, ignoreCase = true) }
            )
            RangeSlider(
                value = filters.scoreRange,
                onValueChange = onScoreRangeChange,
                onValueChangeFinished = onScoreRangeCommit,
                label = "${strings.animeList.scoreLabel}: ${filters.scoreRange.start.roundToInt()} – ${filters.scoreRange.endInclusive.roundToInt()}",
                valueRange = 1f..10f,
                steps = 8
            )
        }
    }
}
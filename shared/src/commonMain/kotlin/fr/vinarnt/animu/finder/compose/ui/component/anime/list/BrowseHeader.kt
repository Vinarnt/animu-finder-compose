package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.theme.SectionTitleTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters
import fr.vinarnt.jikan4k.apis.AnimeApi

@Composable
internal fun BrowseHeader(
    filters: AnimeSearchFilters,
    resultCount: Int?,
    onClearFilters: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTypeChange: (AnimeApi.TypeGetAnime?) -> Unit,
    onStatusChange: (AnimeApi.StatusGetAnime?) -> Unit,
    onRatingChange: (AnimeApi.RatingGetAnime?) -> Unit,
    onGenresChange: (Set<AnimeGenre>) -> Unit,
    onScoreRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onScoreRangeCommit: () -> Unit,
    onSortChange: (AnimeApi.OrderByGetAnime, AnimeApi.SortGetAnime) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = strings.animeList.browseTitle,
            style = SectionTitleTypography,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AnimeListFilters(
            filters = filters,
            onQueryChange = onQueryChange,
            onTypeChange = onTypeChange,
            onStatusChange = onStatusChange,
            onRatingChange = onRatingChange,
            onGenresChange = onGenresChange,
            onScoreRangeChange = onScoreRangeChange,
            onScoreRangeCommit = onScoreRangeCommit,
            onSortChange = onSortChange,
        )

        BrowseResultsRow(
            count = resultCount,
            filtersActive = filters.isActive(),
            onClear = onClearFilters,
        )
    }
}

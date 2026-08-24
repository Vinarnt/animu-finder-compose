package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.ui.component.base.input.RangeSliderControl
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.ExposedSearchableDropDownMenu
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.BaseTextField
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.SearchBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters
import fr.vinarnt.jikan4k.apis.AnimeApi
import kotlin.math.roundToInt

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
                text = "${strings.animeList.scoreLabel}: ${filters.scoreRange.start.roundToInt()} – ${filters.scoreRange.endInclusive.roundToInt()}"
            )
        }
    }
}

@Composable
private fun ScoreFilterChip(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onValueChangeFinished: () -> Unit,
    text: String,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = value != 1f..10f,
            onClick = { expanded = true },
            label = { Text(text) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .onSizeChanged { anchorWidth = it.width }
        )
        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            matchAnchorWidth = false,
            minWidth = with(density) { anchorWidth.toDp() }.coerceAtLeast(Size.Input.minWidth)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "${value.start.roundToInt()} – ${value.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                RangeSliderControl(
                    value = value,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = 1f..10f,
                    steps = 8
                )
            }
        }
    }
}

@Composable
private fun <T> SingleSelectChip(
    text: String,
    selected: Boolean,
    options: List<T>,
    displayOption: @Composable (T) -> String,
    onValueChange: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(text) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .onSizeChanged { anchorWidth = it.width }
        )
        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            matchAnchorWidth = false,
            minWidth = with(density) { anchorWidth.toDp() }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayOption(option)) },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> MultiSelectChip(
    text: String,
    selected: Boolean,
    options: List<T>,
    selectedItems: Set<T>,
    displayOption: @Composable (T) -> String,
    filter: (T, String) -> Boolean,
    onToggle: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val filteredOptions by remember(options, searchText) {
        derivedStateOf {
            if (searchText.isBlank()) options else options.filter { filter(it, searchText) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(text) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .onSizeChanged { anchorWidth = it.width }
        )
        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchText = ""
            },
            matchAnchorWidth = false,
            minWidth = with(density) { anchorWidth.toDp() },
            headerContent = {
                BaseTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text(strings.ui.dropdown.search) },
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )
            }
        ) {
            if (filteredOptions.isEmpty()) {
                Text(
                    strings.ui.dropdown.noContent,
                    modifier = Modifier.padding(Spacing.sm).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(displayOption(option)) },
                        leadingIcon = {
                            Checkbox(
                                checked = option in selectedItems,
                                onCheckedChange = null
                            )
                        },
                        onClick = { onToggle(option) }
                    )
                }
            }
        }
    }
}

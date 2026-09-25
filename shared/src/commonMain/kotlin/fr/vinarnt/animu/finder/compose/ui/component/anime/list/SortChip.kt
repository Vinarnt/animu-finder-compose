package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipBorder
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipColors
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.ExposedSearchableDropDownMenu
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.jikan4k.apis.AnimeApi

/** A filter chip that opens the sort-order dropdown. */
@Composable
internal fun SortChip(
    orderBy: AnimeApi.OrderByGetAnime,
    sort: AnimeApi.SortGetAnime,
    onSortChange: (AnimeApi.OrderByGetAnime, AnimeApi.SortGetAnime) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val sorts = strings.animeList
    val options = buildList {
        add(SortSpec(strings.home.topRated, AnimeApi.OrderByGetAnime.SCORE, AnimeApi.SortGetAnime.DESC))
        add(SortSpec(sorts.sortNewest, AnimeApi.OrderByGetAnime.START_DATE, AnimeApi.SortGetAnime.DESC))
        add(SortSpec(sorts.sortOldest, AnimeApi.OrderByGetAnime.START_DATE, AnimeApi.SortGetAnime.ASC))
        add(SortSpec(sorts.sortAlphabetical, AnimeApi.OrderByGetAnime.TITLE, AnimeApi.SortGetAnime.ASC))
    }
    val current = options.firstOrNull { it.matches(orderBy, sort) } ?: options.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = current.orderBy != AnimeApi.OrderByGetAnime.SCORE || current.sort != AnimeApi.SortGetAnime.DESC,
            onClick = { expanded = true },
            label = { Text("${sorts.sortLabel} · ${current.label}") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .onSizeChanged { anchorWidth = it.width }
                .pointerHoverIcon(PointerIcon.Hand)
                .height(Size.TouchTarget.min),
            colors = filterChipColors(),
            border = filterChipBorder(),
        )
        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            matchAnchorWidth = false,
            minWidth = with(density) { anchorWidth.toDp() }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSortChange(option.orderBy, option.sort)
                    }
                )
            }
        }
    }
}

private data class SortSpec(
    val label: String,
    val orderBy: AnimeApi.OrderByGetAnime,
    val sort: AnimeApi.SortGetAnime,
) {
    fun matches(otherOrder: AnimeApi.OrderByGetAnime, otherSort: AnimeApi.SortGetAnime): Boolean =
        orderBy == otherOrder && sort == otherSort
}

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
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipBorder
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipColors
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.ExposedSearchableDropDownMenu
import fr.vinarnt.animu.finder.compose.ui.theme.Size

/** A filter chip that opens a single-choice dropdown. */
@Composable
internal fun <T> SingleSelectChip(
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

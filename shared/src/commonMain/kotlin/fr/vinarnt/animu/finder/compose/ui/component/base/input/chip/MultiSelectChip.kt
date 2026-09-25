package fr.vinarnt.animu.finder.compose.ui.component.base.input.chip

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
import androidx.compose.ui.text.style.TextAlign
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.ExposedSearchableDropDownMenu
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.BaseTextField
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** A chip that opens a searchable multi-choice dropdown, toggling items via [onToggle]. */
@Composable
internal fun <T> MultiSelectChip(
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
                .pointerHoverIcon(PointerIcon.Hand)
                .height(Size.TouchTarget.min),
            colors = filterChipColors(),
            border = filterChipBorder(),
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

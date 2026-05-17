package fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.BaseTextField
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun <T> MultiSelectDropdown(
    selectedItems: Set<T>,
    options: List<T>,
    label: String,
    onSelectionChange: (Set<T>) -> Unit,
    displayOption: @Composable (T) -> String,
    displaySummary: @Composable (Set<T>) -> String,
    filter: (T, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val filteredOptions by remember(options, searchText) {
        derivedStateOf {
            if (searchText.isBlank()) options
            else options.filter { filter(it, searchText) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(CornerRadius.md))
                .width(Size.Input.minWidth)
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displaySummary(selectedItems),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchText = ""
            }
        ) {
            BaseTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(strings.ui.dropdown.search) }
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

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
                        onClick = {
                            onSelectionChange(
                                if (option in selectedItems) selectedItems - option
                                else selectedItems + option
                            )
                        }
                    )
                }
            }
        }
    }
}
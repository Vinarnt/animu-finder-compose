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
import androidx.compose.ui.text.style.TextAlign
import fr.vinarnt.animu.finder.compose.ui.component.base.input.RangeSliderControl
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipBorder
import fr.vinarnt.animu.finder.compose.ui.component.base.input.chip.filterChipColors
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.ExposedSearchableDropDownMenu
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlin.math.roundToInt

/** A filter chip that opens a score-range slider. */
@Composable
internal fun ScoreFilterChip(
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
                .pointerHoverIcon(PointerIcon.Hand)
                .height(Size.TouchTarget.min),
            colors = filterChipColors(),
            border = filterChipBorder(),
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

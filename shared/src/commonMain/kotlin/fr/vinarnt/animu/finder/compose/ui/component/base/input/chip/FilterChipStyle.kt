package fr.vinarnt.animu.finder.compose.ui.component.base.input.chip

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Shared colors for text/selector chips. */
@Composable
internal fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
)

/** Shared border for text/selector chips. */
@Composable
internal fun filterChipBorder() = FilterChipDefaults.filterChipBorder(
    enabled = true,
    selected = false,
    borderColor = MaterialTheme.colorScheme.outlineVariant,
    selectedBorderColor = MaterialTheme.colorScheme.outlineVariant,
)

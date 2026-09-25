package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** Left/right scroll arrows for a shelf, enabled based on [listState]. */
@Composable
internal fun ShelfNavControls(
    listState: LazyListState,
    onScroll: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShelfNavButton(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = strings.common.scrollLeft,
            enabled = listState.canScrollBackward,
            onClick = { onScroll(false) },
        )
        ShelfNavButton(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = strings.common.scrollRight,
            enabled = listState.canScrollForward,
            onClick = { onScroll(true) },
        )
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.theme.ShelfTitleTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** A shelf header: title (fills the row), scroll controls and an optional "See all" action. */
@Composable
internal fun ShelfHeader(
    title: String,
    seeAll: Boolean,
    onSeeAll: (() -> Unit)?,
    listState: LazyListState,
    onScroll: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = title,
            style = ShelfTitleTypography,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        ShelfNavControls(
            listState = listState,
            onScroll = onScroll,
        )
        if (seeAll && onSeeAll != null) {
            ShelfSeeAllAction(onClick = onSeeAll)
        }
    }
}

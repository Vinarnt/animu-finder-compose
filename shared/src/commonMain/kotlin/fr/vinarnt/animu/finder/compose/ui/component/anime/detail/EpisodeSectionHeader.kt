package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.ui.theme.ShelfTitleTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** "Episodes · N" title row with the episode display-style selector. */
@Composable
internal fun EpisodeSectionHeader(
    title: String,
    selected: EpisodeDisplay,
    onSelect: (EpisodeDisplay) -> Unit,
    gutter: Dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = gutter, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ShelfTitleTypography,
            modifier = Modifier.weight(1f)
        )
        EpisodeDisplaySelector(
            selected = selected,
            onSelect = onSelect
        )
    }
}

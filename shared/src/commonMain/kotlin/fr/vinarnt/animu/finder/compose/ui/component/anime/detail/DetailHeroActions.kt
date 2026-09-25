package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import fr.vinarnt.animu.finder.compose.ui.component.AddToMyListButton
import fr.vinarnt.animu.finder.compose.ui.component.HeroCtaButton
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/**
 * The banner's dark-glass CTA row: Watch (when [onWatch] is set) plus Add-to-My-List
 * (when [onToggleMyList] is set). Renders nothing when both are absent.
 */
@Composable
internal fun DetailHeroActions(
    onWatch: (() -> Unit)?,
    watchLabel: String?,
    inMyList: Boolean,
    onToggleMyList: (() -> Unit)?,
    compact: Boolean,
) {
    if (onWatch != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroCtaButton(onClick = onWatch, label = watchLabel, compact = compact)
            if (onToggleMyList != null) {
                AddToMyListButton(
                    inList = inMyList,
                    compact = compact,
                    onClick = onToggleMyList,
                )
            }
        }
    } else if (onToggleMyList != null) {
        AddToMyListButton(
            inList = inMyList,
            compact = compact,
            onClick = onToggleMyList,
        )
    }
}

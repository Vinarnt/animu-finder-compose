package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** Centered pagination progress indicator for the episode lists. */
@Composable
internal fun EpisodePageProgress(size: Dp) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(size))
    }
}

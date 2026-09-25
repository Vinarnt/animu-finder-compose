package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/** Centered retry action shown when loading an episode page fails. */
@Composable
internal fun EpisodePageError(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = strings.common.retry)
        }
    }
}

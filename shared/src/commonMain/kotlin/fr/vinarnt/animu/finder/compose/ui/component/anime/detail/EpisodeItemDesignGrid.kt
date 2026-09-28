package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner

/**
 * Episodes grid item.
 */
@Composable
fun EpisodeItemDesignGrid(
    episode: GetAnimeByIdEpisodes200ResponseDataInner,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isActive: Boolean = false,
    fallbackImageUrl: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.md),
        color = if (isHovered) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (isHovered) Elevation.md else Elevation.sm,
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EpisodeGridThumbnail(episode, fallbackImageUrl = fallbackImageUrl)
            EpisodeGridCaption(episode = episode, isActive = isActive)
        }
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun EpisodeDisplaySelector(
    selected: EpisodeDisplay,
    onSelect: (EpisodeDisplay) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        EpisodeDisplay.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(CornerRadius.sm))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onSelect(option) }
                    .size(40.dp)
                    .padding(Spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                EpisodeDisplayPreview(
                    display = option,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun EpisodeDisplayPreview(
    display: EpisodeDisplay,
    tint: Color,
) {
    val muted = tint.copy(alpha = 0.5f)
    when (display) {
        EpisodeDisplay.MINIMAL -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(tint, CircleShape)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(5.dp)
                        .background(tint, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(3.dp)
                        .background(muted, RoundedCornerShape(1.dp))
                )
            }
        }

        EpisodeDisplay.POSTER -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(18.dp)
                    .background(tint, RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(muted, RoundedCornerShape(1.dp))
            )
        }
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import fr.vinarnt.animu.finder.compose.ui.component.base.Tooltip
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun AnimeDetailMetric(
    icon: ImageVector,
    value: String,
    description: String
) {
    Tooltip(
        tooltip = { Text(description) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.Icon.sm),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
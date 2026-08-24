package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun AnimeStatusItem(
    status: String?,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (status) {
        "Finished Airing" -> Icons.Default.CheckCircle to "Finished"
        "Currently Airing" -> Icons.Default.PlayCircle to "Airing"
        "Not yet aired" -> Icons.Default.Schedule to "Not yet aired"
        else -> Icons.Default.Schedule to "N/A"
    }
    val iconSize = with(LocalDensity.current) { LocalTextStyle.current.fontSize.toDp() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(text = label)
    }
}
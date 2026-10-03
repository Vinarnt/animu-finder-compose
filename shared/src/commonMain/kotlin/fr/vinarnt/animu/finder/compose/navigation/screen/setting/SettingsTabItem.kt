package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsRow
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius

@Composable
internal fun SettingsTabItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    SettingsRow(
        onClick = onClick,
        modifier = Modifier.background(
            color = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
            shape = RoundedCornerShape(CornerRadius.sm),
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

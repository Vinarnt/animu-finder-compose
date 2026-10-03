package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.AppHorizontalDivider
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsGroup
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsRow
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsRowTitle
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
internal fun SectionChooser(
    sections: List<SettingsSection>,
    onOpen: (SettingsSection) -> Unit,
) {
    SettingsGroup(title = strings.settings.title.uppercase()) {
        sections.forEach { section ->
            if (section.label == strings.settings.debug) {
                AppHorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
            }
            SettingsRow(onClick = { onOpen(section) }) {
                SettingsRowTitle(section.label)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

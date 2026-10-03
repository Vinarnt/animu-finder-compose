package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.AppHorizontalDivider
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsGroup
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
internal fun SettingsTabsLayout(
    sections: List<SettingsSection>,
    navigation: SettingsNavigation,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .padding(top = Spacing.lg)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(
            space = Spacing.xl,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(TabStripWidth),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            sections.forEach { section ->
                if (section.label == strings.settings.debug) {
                    AppHorizontalDivider()
                }
                SettingsTabItem(
                    title = section.label,
                    icon = section.icon,
                    selected = section === navigation.open,
                    onClick = { navigation.open = section },
                )
            }
        }

        Column(modifier = Modifier.widthIn(max = SectionMaxWidth)) {
            navigation.open?.let { section ->
                SettingsGroup(title = section.label.uppercase()) { section.content() }
            }
        }
    }
}

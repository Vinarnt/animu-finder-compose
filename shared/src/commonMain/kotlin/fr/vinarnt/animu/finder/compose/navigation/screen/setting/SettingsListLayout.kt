package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsGroup
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
internal fun SettingsListLayout(
    sections: List<SettingsSection>,
    navigation: SettingsNavigation,
) {
    val section = navigation.open

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .padding(top = Spacing.lg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.widthIn(max = SectionMaxWidth)) {
            if (section == null) {
                SectionChooser(sections) { navigation.open = it }
            } else {
                SettingsGroup(title = section.label.uppercase()) { section.content() }
            }
        }
    }
}

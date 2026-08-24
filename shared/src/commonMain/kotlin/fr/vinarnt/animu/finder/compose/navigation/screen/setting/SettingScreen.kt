package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown.Select
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.dropdown.LanguageDropdown
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class SettingScreen : Screen {

    override val key: ScreenKey = "settings"

    @Composable
    override fun Content() {
        val settingManager = koinInject<SettingManager>()

        MainLayout(
            topBar = { NavigationBar(strings.settings.title) }
        ) {
            Column(
                modifier = Modifier.width(Size.maxContentWidth).padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SectionTitle(strings.settings.appearance)
                ThemeSelect(settingManager)
                LanguageSelect()

                SectionTitle(strings.settings.playback)
                DefaultQualitySelect(settingManager)
                PreferredSubtitlesSelect(settingManager)
                AutoplayNextSwitch(settingManager)
            }
        }
    }

    @Composable
    private fun SectionTitle(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

    @Composable
    private fun ThemeSelect(settingManager: SettingManager) {
        val theme = settingManager.getTheme().collectAsStateWithLifecycle(Theme.AUTO)
        val options = listOf(Theme.AUTO, Theme.LIGHT, Theme.DARK)

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = strings.settings.theme.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == theme.value,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = {
                            CoroutineScope(Dispatchers.Default).launch {
                                settingManager.setTheme(option)
                            }
                        }
                    ) {
                        Text(
                            text = when (option) {
                                Theme.AUTO -> strings.settings.theme.values.system
                                Theme.LIGHT -> strings.settings.theme.values.light
                                Theme.DARK -> strings.settings.theme.values.dark
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LanguageSelect() {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = strings.settings.locale.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LanguageDropdown()
        }
    }

    @Composable
    private fun DefaultQualitySelect(settingManager: SettingManager) {
        val quality = settingManager.getDefaultQuality().collectAsStateWithLifecycle("Auto")
        val options = listOf("Auto", "1080p", "720p", "480p")

        Select(
            selectedValue = quality.value,
            options = options,
            label = strings.settings.defaultQuality,
            onValueChangedEvent = {
                CoroutineScope(Dispatchers.Default).launch {
                    settingManager.setDefaultQuality(it)
                }
            },
            displayOption = { it }
        )
    }

    @Composable
    private fun PreferredSubtitlesSelect(settingManager: SettingManager) {
        val subtitles = settingManager.getPreferredSubtitles().collectAsStateWithLifecycle("English")
        val options = listOf("English", "French", "Japanese", "None")

        Select(
            selectedValue = subtitles.value,
            options = options,
            label = strings.settings.preferredSubtitles,
            onValueChangedEvent = {
                CoroutineScope(Dispatchers.Default).launch {
                    settingManager.setPreferredSubtitles(it)
                }
            },
            displayOption = { it }
        )
    }

    @Composable
    private fun AutoplayNextSwitch(settingManager: SettingManager) {
        val autoplayNext = settingManager.getAutoplayNext().collectAsStateWithLifecycle(true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.settings.autoplayNext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = autoplayNext.value,
                onCheckedChange = {
                    CoroutineScope(Dispatchers.Default).launch {
                        settingManager.setAutoplayNext(it)
                    }
                }
            )
        }
    }
}

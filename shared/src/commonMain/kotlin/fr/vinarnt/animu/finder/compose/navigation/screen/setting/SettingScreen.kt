package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsAutoplaySwitch
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsGroup
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsLanguageSelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsQualitySelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsSubtitlesSelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsThemeSelect
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel


class SettingScreen : Screen {

    override val key: ScreenKey = "settings"

    @Composable
    override fun Content() {
        val vm: SettingsViewModel = koinViewModel()
        val theme by vm.theme.collectAsStateWithLifecycle()
        val locale by vm.locale.collectAsStateWithLifecycle()
        val quality by vm.defaultQuality.collectAsStateWithLifecycle()
        val subtitles by vm.preferredSubtitles.collectAsStateWithLifecycle()
        val autoplayNext by vm.autoplayNext.collectAsStateWithLifecycle()

        MainLayout(
            topBar = { NavigationBar(strings.settings.title) }
        ) {
            Column(
                modifier = Modifier
                    .width(Size.maxContentWidth)
                    .padding(horizontal = Spacing.md)
                    .padding(top = 28.dp, bottom = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                SettingsGroup(title = strings.settings.appearance.uppercase()) {
                    SettingsThemeSelect(selected = theme, onSelect = vm::setTheme)
                }

                SettingsGroup(title = strings.settings.general.uppercase()) {
                    SettingsLanguageSelect(currentLocale = locale, onSelect = vm::setLocale)
                }

                SettingsGroup(title = strings.settings.playback.uppercase()) {
                    SettingsQualitySelect(quality = quality, onSelect = vm::setDefaultQuality)
                    SettingsSubtitlesSelect(subtitles = subtitles, onSelect = vm::setPreferredSubtitles)
                    SettingsAutoplaySwitch(enabled = autoplayNext, onChange = vm::setAutoplayNext)
                }
            }
        }
    }
}

package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsAutoplaySwitch
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsCloudflareClearances
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsLanguageSelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsQualitySelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsSubtitlesSelect
import fr.vinarnt.animu.finder.compose.ui.component.setting.SettingsThemeSelect
import fr.vinarnt.animu.finder.compose.viewmodel.SettingsViewModel

internal data class SettingsSection(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit,
)

internal class SettingsNavigation {
    var open: SettingsSection? by mutableStateOf(null)
}

@Composable
internal fun settingsSections(
    vm: SettingsViewModel,
    onOpenCloudflare: (label: String, host: String) -> Unit = { _, _ -> },
): List<SettingsSection> {
    val s = strings.settings
    val theme by vm.theme.collectAsStateWithLifecycle()
    val locale by vm.locale.collectAsStateWithLifecycle()
    val quality by vm.defaultQuality.collectAsStateWithLifecycle()
    val subtitles by vm.preferredSubtitles.collectAsStateWithLifecycle()
    val autoplayNext by vm.autoplayNext.collectAsStateWithLifecycle()
    val cloudflareClearances by vm.cloudflareClearances.collectAsStateWithLifecycle()

    return remember(
        s, theme, locale, quality, subtitles, autoplayNext, cloudflareClearances, vm,
    ) {
        listOf(
            SettingsSection(
                label = s.general,
                icon = Icons.Default.Tune,
                content = {
                    SettingsLanguageSelect(
                        currentLocale = locale,
                        onSelect = vm::setLocale,
                    )
                },
            ),
            SettingsSection(
                label = s.appearance,
                icon = Icons.Default.Palette,
                content = { SettingsThemeSelect(selected = theme, onSelect = vm::setTheme) },
            ),
            SettingsSection(
                label = s.playback,
                icon = Icons.Default.PlayArrow,
                content = {
                    SettingsQualitySelect(quality = quality, onSelect = vm::setDefaultQuality)
                    SettingsSubtitlesSelect(
                        subtitles = subtitles,
                        onSelect = vm::setPreferredSubtitles,
                    )
                    SettingsAutoplaySwitch(
                        enabled = autoplayNext,
                        onChange = vm::setAutoplayNext,
                    )
                },
            ),
            SettingsSection(
                label = s.debug,
                icon = Icons.Default.BugReport,
                content = {
                    SettingsCloudflareClearances(
                        clearances = cloudflareClearances,
                        onOpen = { site -> onOpenCloudflare(site.label, site.host) },
                    )
                },
            ),
        )
    }
}

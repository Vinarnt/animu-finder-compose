package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Theme

/** Theme chooser (Light / Dark / System) rendered as radio rows. */
@Composable
internal fun SettingsThemeSelect(selected: Theme, onSelect: (Theme) -> Unit) {
    val options = listOf(
        Theme.LIGHT to strings.settings.theme.values.light,
        Theme.DARK to strings.settings.theme.values.dark,
        Theme.AUTO to strings.settings.theme.values.system,
    )

    options.forEach { (option, label) ->
        SettingsRow(
            onClick = { onSelect(option) },
            trailing = { SettingsRadioIndicator(selected = option == selected) },
        ) {
            SettingsRowTitle(label)
        }
    }
}

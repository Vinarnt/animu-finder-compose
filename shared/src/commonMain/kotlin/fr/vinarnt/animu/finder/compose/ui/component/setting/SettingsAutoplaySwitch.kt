package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings

/** Autoplay-next-episode toggle row. */
@Composable
internal fun SettingsAutoplaySwitch(enabled: Boolean, onChange: (Boolean) -> Unit) {
    SettingsRow(
        onClick = { onChange(!enabled) },
        trailing = {
            Switch(
                checked = enabled,
                onCheckedChange = onChange,
            )
        },
    ) {
        SettingsRowTitle(strings.settings.autoplayNext)
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings

/** Language chooser backed by the available locales. */
@Composable
internal fun SettingsLanguageSelect(currentLocale: String, onSelect: (String) -> Unit) {
    val localeStrings = strings.languages.locales
    val items = localeStrings.map { it to strings.languages.getLocaleLabel(it) }
    val selected = items.firstOrNull { it.first == currentLocale } ?: items.first()

    SettingsSelectRow(
        title = strings.settings.locale.label,
        selected = selected,
        options = items,
        onSelected = { onSelect(it.first) },
    )
}

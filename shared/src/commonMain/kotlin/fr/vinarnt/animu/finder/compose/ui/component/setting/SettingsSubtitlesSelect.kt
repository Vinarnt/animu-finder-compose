package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings

/** Preferred subtitle language chooser. */
@Composable
internal fun SettingsSubtitlesSelect(subtitles: String, onSelect: (String) -> Unit) {
    val english = strings.settings.subtitleLanguageEnglish
    val options = listOf(
        english,
        strings.settings.subtitleLanguageFrench,
        strings.settings.subtitleLanguageJapanese,
        strings.settings.subtitleLanguageNone,
    ).map { it to it }
    val selected = options.firstOrNull { it.first == subtitles } ?: options.first()

    SettingsSelectRow(
        title = strings.settings.preferredSubtitles,
        selected = selected,
        options = options,
        onSelected = { onSelect(it.first) },
    )
}

package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings

/** Default playback quality chooser. */
@Composable
internal fun SettingsQualitySelect(quality: String, onSelect: (String) -> Unit) {
    val autoLabel = strings.settings.qualityAuto
    val options = listOf(autoLabel, "1080p", "720p", "480p").map { it to it }
    val selected = options.firstOrNull { it.first == quality } ?: options.first()

    SettingsSelectRow(
        title = strings.settings.defaultQuality,
        selected = selected,
        options = options,
        onSelected = { onSelect(it.first) },
    )
}

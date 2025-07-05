package fr.vinarnt.animu.finder.compose.service

import androidx.compose.ui.text.intl.Locale
import cafe.adriel.lyricist.LanguageTag
import com.russhwolf.settings.coroutines.FlowSettings
import fr.vinarnt.animu.finder.compose.ui.theme.Theme
import kotlinx.coroutines.flow.transform

class SettingManager(val settings: FlowSettings) {

    fun getTheme() = settings.getStringFlow("theme", Theme.AUTO.name)
        .transform { emit(Theme.valueOf(it)) }

    suspend fun setTheme(theme: Theme) = settings.putString("theme", theme.name)

    fun getLocale() = settings.getStringFlow("locale", Locale.current.language)

    suspend fun setLocale(locale: LanguageTag) = settings.putString("locale", locale)
}

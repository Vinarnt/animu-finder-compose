package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vinarnt.animu.finder.compose.model.CloudflareClearance
import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.ui.theme.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-wide user settings (theme, locale, playback preferences). Owns all
 * [SettingManager] reads/writes so composables can stay declarative.
 */
class SettingsViewModel(
    private val settingManager: SettingManager,
    private val cloudflareClearanceStore: CloudflareClearanceStore,
) : ViewModel() {

    val theme: StateFlow<Theme> = settingManager.getTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme.AUTO)

    val locale: StateFlow<String> = settingManager.getLocale()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Locale.current.language)

    val defaultQuality: StateFlow<String> = settingManager.getDefaultQuality()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_QUALITY)

    val preferredSubtitles: StateFlow<String> = settingManager.getPreferredSubtitles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_SUBTITLES)

    val autoplayNext: StateFlow<Boolean> = settingManager.getAutoplayNext()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val cloudflareClearances: StateFlow<Map<String, CloudflareClearance>> =
        cloudflareClearanceStore.clearances()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun setTheme(theme: Theme) {
        viewModelScope.launch { settingManager.setTheme(theme) }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch { settingManager.setLocale(locale) }
    }

    fun setDefaultQuality(quality: String) {
        viewModelScope.launch { settingManager.setDefaultQuality(quality) }
    }

    fun setPreferredSubtitles(subtitles: String) {
        viewModelScope.launch { settingManager.setPreferredSubtitles(subtitles) }
    }

    fun setAutoplayNext(enabled: Boolean) {
        viewModelScope.launch { settingManager.setAutoplayNext(enabled) }
    }

    fun saveCloudflareClearance(host: String, cookie: String, userAgent: String) {
        viewModelScope.launch {
            cloudflareClearanceStore.save(host, cookie, userAgent)
        }
    }

    fun removeCloudflareClearance(host: String) {
        viewModelScope.launch {
            cloudflareClearanceStore.remove(host)
        }
    }

    companion object {
        const val DEFAULT_QUALITY = "Auto"
        const val DEFAULT_SUBTITLES = "English"
    }
}

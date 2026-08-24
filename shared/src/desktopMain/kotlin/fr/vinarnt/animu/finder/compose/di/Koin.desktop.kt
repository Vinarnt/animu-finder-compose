package fr.vinarnt.animu.finder.compose.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import org.koin.dsl.module
import java.util.prefs.Preferences

actual val platformModule = module {
    single<SettingManager> {
        SettingManager(PreferencesSettings(Preferences.userRoot()).toFlowSettings())
    }
}

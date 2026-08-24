package fr.vinarnt.animu.finder.compose.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import org.koin.dsl.module

internal actual val platformModule = module {
    single<SettingManager> {
        SettingManager(
            NSUserDefaultsSettings(
                platform.Foundation.NSUserDefaults.standardUserDefaults
            ).toFlowSettings()
        )
    }
}

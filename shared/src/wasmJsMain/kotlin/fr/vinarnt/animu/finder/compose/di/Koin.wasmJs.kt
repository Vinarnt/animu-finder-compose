package fr.vinarnt.animu.finder.compose.di

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.observable.makeObservable
import fr.vinarnt.animu.finder.compose.service.SettingManager
import org.koin.dsl.module

internal actual val platformModule = module {
    single<SettingManager> {
        SettingManager(
            StorageSettings().makeObservable().toFlowSettings()
        )
    }
}

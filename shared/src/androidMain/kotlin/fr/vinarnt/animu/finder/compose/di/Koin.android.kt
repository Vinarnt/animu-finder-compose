package fr.vinarnt.animu.finder.compose.di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent

actual val platformModule = module {
    single<SettingManager> {
        val context: Context by KoinJavaComponent.inject(Context::class.java)

        SettingManager(
            SharedPreferencesSettings(
                context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            ).toFlowSettings()
        )
    }
}

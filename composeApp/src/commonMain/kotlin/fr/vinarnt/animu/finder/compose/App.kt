package fr.vinarnt.animu.finder.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import fr.vinarnt.animu.finder.compose.di.koinAppDeclaration
import fr.vinarnt.animu.finder.compose.i18n.LocalStrings
import fr.vinarnt.animu.finder.compose.i18n.Locales
import fr.vinarnt.animu.finder.compose.i18n.StringsMap
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.list.AnimeListScreen
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.ui.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinConfiguration

@OptIn(ExperimentalVoyagerApi::class)
@Composable
@Preview
fun App(koinAppDeclaration: KoinAppDeclaration = {}) {
    KoinApplication(configuration = koinConfiguration(koinAppDeclaration(koinAppDeclaration)), content = {
            val settingManager = koinInject<SettingManager>()
            val locale = settingManager.getLocale().collectAsStateWithLifecycle(Locales.EN)
            val lyricist = rememberStrings(StringsMap, currentLanguageTag = locale.value)

            ProvideStrings(lyricist, LocalStrings) {
                AppTheme {
                    Navigator(screen = AnimeListScreen()) { navigator ->
                        CurrentScreen()
                    }
                }
            }
        })
}

package fr.vinarnt.animu.finder.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import fr.vinarnt.animu.finder.compose.di.koinAppDeclaration
import fr.vinarnt.animu.finder.compose.i18n.LocalStrings
import fr.vinarnt.animu.finder.compose.i18n.StringsMap
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.list.AnimeListScreen
import fr.vinarnt.animu.finder.compose.ui.component.setting.CloudflareChallengeHost
import fr.vinarnt.animu.finder.compose.ui.theme.AppTheme
import fr.vinarnt.animu.finder.compose.ui.theme.ProvideWindowWidth
import fr.vinarnt.animu.finder.compose.viewmodel.SettingsViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App(koinAppDeclaration: KoinAppDeclaration = {}) {
    KoinApplication(configuration = koinConfiguration(koinAppDeclaration(koinAppDeclaration)), content = {
        val settingsVm: SettingsViewModel = koinViewModel()
        val locale by settingsVm.locale.collectAsStateWithLifecycle()
        val theme by settingsVm.theme.collectAsStateWithLifecycle()
        val lyricist = rememberStrings(StringsMap, currentLanguageTag = locale)

        ProvideStrings(lyricist, LocalStrings) {
            AppTheme(theme = theme) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    ProvideWindowWidth {
                        Box {
                            Navigator(screen = AnimeListScreen()) { navigator ->
                                CurrentScreen()
                            }
                            CloudflareChallengeHost()
                        }
                    }
                }
            }
        }
    })
}

package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.dropdown.LanguageDropdown
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class SettingScreen : Screen {

    override val key: ScreenKey = "settings"

    @Composable
    override fun Content() {
        val settingManager = koinInject<SettingManager>()

        MainLayout(
            topBar = { NavigationBar(strings.settings.title) }
        ) {
            Column(Modifier.padding(16.dp)) {
                ThemeSelect(settingManager)
                LanguageSelect()
            }
        }
    }

    @Composable
    private fun ThemeSelect(settingManager: SettingManager) {
        val theme = settingManager.getTheme().collectAsStateWithLifecycle(Theme.AUTO)
        val options = listOf(Theme.AUTO, Theme.LIGHT, Theme.DARK)

        Column {
            Text(
                text = strings.settings.theme.label
            )
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == theme.value,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = {
                            CoroutineScope(Dispatchers.Default).launch {
                                settingManager.setTheme(option)
                            }
                        }
                    ) {
                        Text(
                            text = when (option) {
                                Theme.AUTO -> strings.settings.theme.values.system
                                Theme.LIGHT -> strings.settings.theme.values.light
                                Theme.DARK -> strings.settings.theme.values.dark
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LanguageSelect() {
        Column {
            Text(
                text = strings.settings.locale.label
            )
            LanguageDropdown()
        }
    }
}

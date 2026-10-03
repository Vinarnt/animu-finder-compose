package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel


class SettingScreen : Screen {

    override val key: ScreenKey = "settings"

    @Composable
    override fun Content() {
        val vm: SettingsViewModel = koinViewModel()
        val navigator = LocalNavigator.currentOrThrow

        val back: () -> Unit = { navigator.pop() }
        var sectionLabel by remember { mutableStateOf<String?>(null) }
        var leaveSection: (() -> Unit)? by remember { mutableStateOf(null) }
        val section = sectionLabel
        val sectionBack = leaveSection

        MainLayout(
            topBar = {
                NavigationBar(
                    title = section ?: strings.settings.title,
                    onBack = if (section != null && sectionBack != null) sectionBack else back,
                )
            },
            scrollable = false,
        ) {
            SettingsScreen(
                vm = vm,
                onOpenCloudflare = { label, host ->
                    navigator.push(CloudflareClearanceScreen(label, host))
                },
                onSectionChange = { label, leave ->
                    sectionLabel = label
                    leaveSection = leave
                },
            )
        }
    }
}

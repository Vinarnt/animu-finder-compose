package fr.vinarnt.animu.finder.compose.ui.component.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.navigation.screen.setting.SettingScreen
import fr.vinarnt.animu.finder.compose.ui.component.Tooltip

@Composable
fun SettingButton() {
    val navigator = LocalNavigator.currentOrThrow

    Tooltip(
        tooltip = {
            Text(strings.settings.title)
        }
    ) {
        IconButton(
            onClick = {
                navigator += SettingScreen()
            }
        ) {
            Icon(
                Icons.Default.Settings,
                strings.settings.title,
            )
        }
    }
}

package fr.vinarnt.animu.finder.compose.ui.component.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.navigation.screen.setting.SettingScreen
import fr.vinarnt.animu.finder.compose.ui.component.base.Tooltip

@Composable
fun SettingButton(onGlass: Boolean = false) {
    val navigator = LocalNavigator.currentOrThrow

    Tooltip(
        tooltip = {
            Text(strings.settings.title)
        },
        anchorPosition = TooltipAnchorPosition.Below,
    ) {
        GlassIconButton(
            imageVector = Icons.Default.Settings,
            contentDescription = strings.settings.title,
            onClick = {
                navigator += SettingScreen()
            },
            onGlass = onGlass,
        )
    }
}
package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun SettingBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

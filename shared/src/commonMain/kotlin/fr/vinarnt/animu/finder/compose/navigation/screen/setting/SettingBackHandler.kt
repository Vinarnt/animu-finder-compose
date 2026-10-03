package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.runtime.Composable

/**
 * Handles a back press from the system while [enabled], so a screen can consume it before the
 * navigator pops the whole destination.
 *
 * Wired per platform: Android routes the system back gesture here. Desktop and the web have no
 * system back gesture, so this is a no-op there and the in-page affordance is the app bar's back
 * button, which the settings screen wires to the same handler.
 */
@Composable
expect fun SettingBackHandler(enabled: Boolean, onBack: () -> Unit)

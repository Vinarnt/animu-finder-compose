package fr.vinarnt.animu.finder.compose.ui.component.button

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.list.MyListScreen
import fr.vinarnt.animu.finder.compose.navigation.screen.setting.SettingScreen

internal fun Navigator.openAppBarDestination(screen: Screen) {
    val existing = items.lastOrNull { it.key == screen.key }
    when {
        existing != null -> popUntil { it.key == screen.key }
        lastItem.key in AppBarDestinationKeys -> replace(screen)
        else -> push(screen)
    }
}

private val AppBarDestinationKeys: Set<String> = setOf(MyListScreen().key, SettingScreen().key)

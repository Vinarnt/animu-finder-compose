package fr.vinarnt.animu.finder.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.vinarnt.animu.finder.compose.di.appModule

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "animu-finder-compose",
    ) {
        App()
    }
}

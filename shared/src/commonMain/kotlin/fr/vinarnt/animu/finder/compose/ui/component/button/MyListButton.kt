package fr.vinarnt.animu.finder.compose.ui.component.button

import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.list.MyListScreen
import fr.vinarnt.animu.finder.compose.ui.component.BookmarkIcon
import fr.vinarnt.animu.finder.compose.ui.component.base.Tooltip

@Composable
fun MyListButton(onGlass: Boolean = false) {
    val navigator = LocalNavigator.currentOrThrow

    Tooltip(
        tooltip = {
            Text(strings.myList.title)
        },
        anchorPosition = TooltipAnchorPosition.Below,
    ) {
        AppBarIconButton(
            imageVector = BookmarkIcon,
            contentDescription = strings.myList.title,
            onClick = {
                navigator.openAppBarDestination(MyListScreen())
            },
            onGlass = onGlass,
        )
    }
}

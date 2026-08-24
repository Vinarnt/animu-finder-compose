package fr.vinarnt.animu.finder.compose.ui.component.navigation.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings

@Composable
fun NavigationBar(
    title: String? = null,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            if (navigator.canPop) {
                IconButton(
                    onClick = {
                        navigator.pop()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        strings.navigation.back,
                    )
                }
            }
        },
        title = {
            title?.let { Text(it) }
        },
        actions = actions,
    )
}

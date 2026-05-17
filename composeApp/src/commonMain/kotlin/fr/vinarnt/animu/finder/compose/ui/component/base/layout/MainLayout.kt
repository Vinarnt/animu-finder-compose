package fr.vinarnt.animu.finder.compose.ui.component.base.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun MainLayout(
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        content = { paddingValues ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.width(Size.maxContentWidth).padding(Spacing.sm)
                ) {
                    content()
                }
            }
        }
    )
}

package fr.vinarnt.animu.finder.compose.ui.component.base.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

/**
 * Base layout for screens. Owns the Scaffold, top bar and the vertical scroll of the page body.
 *
 * The scrollable body spans the whole available width so scrolling gestures work anywhere on the
 * screen (including the margins). Each screen is responsible for constraining its own content to
 * [fr.vinarnt.animu.finder.compose.ui.theme.Size.maxContentWidth] when needed.
 *
 * @param scrollable whether the page body itself should scroll. Set to `false` for screens that
 * provide their own scrollable content (e.g. paginated lazy lists), to avoid nested scrolling.
 */
@Composable
fun MainLayout(
    topBar: @Composable () -> Unit,
    scrollable: Boolean = true,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.sm)
                    .then(
                        if (scrollable) Modifier.verticalScroll(rememberScrollState())
                        else Modifier
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    )
}

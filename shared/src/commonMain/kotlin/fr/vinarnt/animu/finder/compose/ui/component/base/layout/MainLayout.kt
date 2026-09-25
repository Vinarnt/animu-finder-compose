package fr.vinarnt.animu.finder.compose.ui.component.base.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
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
 * @param overlayTopBar when `true` the top bar is drawn over the top of the content (used by
 * immersive hero/detail screens), instead of pushing content down.
 */
@Composable
fun MainLayout(
    topBar: @Composable () -> Unit,
    scrollable: Boolean = true,
    overlayTopBar: Boolean = false,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { if (!overlayTopBar) topBar() },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            if (overlayTopBar) {
                                PaddingValues(bottom = paddingValues.calculateBottomPadding())
                            } else {
                                paddingValues
                            }
                        )
                        .then(
                            if (overlayTopBar) Modifier
                            else Modifier.padding(Spacing.sm)
                        )
                        .then(
                            if (scrollable) Modifier.verticalScroll(rememberScrollState())
                            else Modifier
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
                if (overlayTopBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(1f)
                    ) {
                        topBar()
                    }
                }
            }
        }
    )
}
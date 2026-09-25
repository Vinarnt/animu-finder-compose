package fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

/**
 * A Material3 `ExposedDropdownMenuBox` menu with an optional searchable header,
 * custom positioning, open/close animation and a scrollable card surface.
 */
@Composable
fun ExposedDropdownMenuBoxScope.ExposedSearchableDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    matchAnchorWidth: Boolean = true,
    minWidth: Dp = Dp.Unspecified,
    headerContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider =
            DropdownMenuPositionProvider(DpOffset.Zero, density) { parentBounds, menuBounds ->
                transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
            }

        ExposedDropdownMenuPopup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider
        ) {
            DropdownMenuContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                scrollState = scrollState,
                modifier = modifier.exposedDropdownSize(matchAnchorWidth),
                minWidth = minWidth,
                headerContent = headerContent,
                content = content
            )
        }
    }
}

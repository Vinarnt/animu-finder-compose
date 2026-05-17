package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Tooltip(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    anchorPosition: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    enableUserInput: Boolean = true,
    content: @Composable () -> Unit
) {
    TooltipBox(
        tooltip = {
            RichTooltip(text = tooltip, caretShape = TooltipDefaults.caretShape())
        },
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(anchorPosition),
        state = rememberTooltipState(),
        enableUserInput = enableUserInput,
        modifier = modifier,
        content = content
    )
}

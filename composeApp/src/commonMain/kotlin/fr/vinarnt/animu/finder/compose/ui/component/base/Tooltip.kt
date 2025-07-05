package fr.vinarnt.animu.finder.compose.ui.component

import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Tooltip(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enableUserInput: Boolean = true,
    content: @Composable () -> Unit
) {
    TooltipBox(
        tooltip = {
            RichTooltip(caretSize = TooltipDefaults.caretSize, text = tooltip)
        },
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        state = rememberTooltipState(),
        enableUserInput = enableUserInput,
        modifier = modifier,
        content = content
    )
}

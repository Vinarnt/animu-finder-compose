package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger

@Composable
fun CollapsingLayout(
    modifier: Modifier = Modifier,
    expandedContent: @Composable (Modifier) -> Unit,
    collapsedContent: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) {

    val localDensity = LocalDensity.current
    var currentHeight by remember { mutableFloatStateOf(0f) }
    var maxHeight by remember { mutableFloatStateOf(-1f) }
    var minHeight by remember { mutableFloatStateOf(-1f) }
    val animationProgress by remember(currentHeight) { mutableFloatStateOf((currentHeight - minHeight) / (maxHeight - minHeight)) }
    Logger.d("Collapsible : currentHeight: $currentHeight, minHeight: $minHeight, maxHeight: $maxHeight, prgoress: $animationProgress")

    LaunchedEffect(maxHeight) {
        if (currentHeight == 0f) {
            currentHeight = maxHeight  // initially expand it
        }
    }

    val nestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (currentHeight != minHeight && available.y < 0) {
                currentHeight = (currentHeight + available.y).coerceAtLeast(minHeight)
                return available
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (currentHeight != maxHeight && available.y > 0) {
                currentHeight = (currentHeight + available.y).coerceAtMost(maxHeight)
                return available
            }
            return Offset.Zero
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .nestedScroll(nestedScrollConnection),
    ) {

        Box(
            modifier = Modifier.padding(8.dp).then(
                if (currentHeight != 0f) {
                    Modifier
                        .height(with(localDensity) { currentHeight.toDp() })
                        .clipToBounds()
                } else {
                    Modifier
                }
            )
        ) {
            expandedContent(
                Modifier.onGloballyPositioned { coordinates ->
                    if (maxHeight == -1f) {
                        maxHeight = coordinates.size.height.toFloat()
                    }
                }.alpha(animationProgress)
            )
            collapsedContent(
                Modifier.onGloballyPositioned { coordinates ->
                    if (minHeight == -1f) {
                        minHeight = coordinates.size.height.toFloat()
                    }
                }.alpha(1 - animationProgress)
            )
        }


        content(Modifier.weight(1f))
    }
}

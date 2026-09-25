package fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.component.base.appVerticalScrollbar
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

private const val InTransitionDuration = 120
private const val OutTransitionDuration = 75

/** The menu's card surface: scale/alpha animation, optional header and scrollable content. */
@Composable
internal fun DropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    minWidth: Dp = Dp.Unspecified,
    headerContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = rememberTransition(expandedStates, "DropDownMenu")

    val scale by
    transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = InTransitionDuration, easing = LinearOutSlowInEasing)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = 1, delayMillis = OutTransitionDuration - 1)
            }
        }
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0.8f
        }
    }

    val alpha by
    transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
    Card(
        modifier =
            Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                transformOrigin = transformOriginState.value
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        BoxWithConstraints(modifier = modifier) {
            val maxMenuHeight = (maxHeight - 96.dp).coerceAtLeast(0.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = minWidth)
                    .width(IntrinsicSize.Max)
                    .heightIn(max = maxMenuHeight)
            ) {
                if (headerContent != null) {
                    headerContent()
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appVerticalScrollbar(scrollState)
                        .padding(vertical = Spacing.sm),
                    content = content
                )
            }
        }
    }
}

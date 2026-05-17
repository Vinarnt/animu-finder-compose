package fr.vinarnt.animu.finder.compose.ui.component.base.input.dropdown

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.BaseTextField
import fr.vinarnt.animu.finder.compose.ui.component.base.input.textfield.TextField
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlin.math.max
import kotlin.math.min

@Composable
fun <T> SearchDropdown(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemContent: @Composable (T) -> Unit,
    displayText: @Composable (T) -> String,
    filter: (T, String) -> Boolean,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = { },
    modifier: Modifier = Modifier,
    noItemsContent: @Composable () -> Unit = {
        Text(
            strings.ui.dropdown.noContent,
            modifier = Modifier.padding(Spacing.sm).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    },
) {
    var expanded by remember { mutableStateOf(expanded) }
    var searchText by remember { mutableStateOf("") }
    val filteredList by remember(items) {
        derivedStateOf {
            items.filter {
                filter(it, searchText)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            onExpandedChange(it)
        },
        modifier = modifier
    ) {
        TextField(
            value = displayText(selectedItem),
            onValueChange = { },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .pointerHoverIcon(PointerIcon.Default, true),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        ExposedSearchableDropDownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onExpandedChange(false)
            }
        ) {
            BaseTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(strings.ui.dropdown.search) },
                modifier = Modifier.border(0.dp, Color.Transparent).fillMaxWidth()
            )

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            if (searchText.isNotEmpty() && filteredList.isEmpty() || items.isEmpty()) {
                noItemsContent()
            } else {
                (filteredList.ifEmpty { items }).forEach { item ->
                    Box(
                        Modifier.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val eventOnFinalPass = awaitPointerEvent(PointerEventPass.Final)
                                    if (eventOnFinalPass.type == PointerEventType.Release) {
                                        expanded = false
                                        onItemSelected(item)
                                    }
                                }
                            }
                        }
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExposedDropdownMenuBoxScope.ExposedSearchableDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
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
                modifier = modifier.exposedDropdownSize(),
                content = content
            )
        }
    }
}

@Immutable
internal data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { 48.dp.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX =
            with(density) {
                contentOffset.x.roundToPx() *
                        (if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
            }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val leftToAnchorLeft = anchorBounds.left + contentOffsetX
        val rightToAnchorRight = anchorBounds.right - popupContentSize.width + contentOffsetX
        val rightToWindowRight = windowSize.width - popupContentSize.width
        val leftToWindowLeft = 0
        val x =
            if (layoutDirection == LayoutDirection.Ltr) {
                sequenceOf(
                    leftToAnchorLeft,
                    rightToAnchorRight,
                    // If the anchor gets outside of the window on the left, we want to position
                    // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                    if (anchorBounds.left >= 0) rightToWindowRight else leftToWindowLeft
                )
            } else {
                sequenceOf(
                    rightToAnchorRight,
                    leftToAnchorLeft,
                    // If the anchor gets outside of the window on the right, we want to
                    // position
                    // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                    if (anchorBounds.right <= windowSize.width) leftToWindowLeft
                    else rightToWindowRight
                )
            }
                .firstOrNull { it >= 0 && it + popupContentSize.width <= windowSize.width }
                ?: rightToAnchorRight

        // Compute vertical position.
        val topToAnchorBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val bottomToAnchorTop = anchorBounds.top - popupContentSize.height + contentOffsetY
        val centerToAnchorTop = anchorBounds.top - popupContentSize.height / 2 + contentOffsetY
        val bottomToWindowBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y =
            sequenceOf(
                topToAnchorBottom,
                bottomToAnchorTop,
                centerToAnchorTop,
                bottomToWindowBottom
            )
                .firstOrNull {
                    it >= verticalMargin &&
                            it + popupContentSize.height <= windowSize.height - verticalMargin
                } ?: bottomToAnchorTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}

internal fun calculateTransformOrigin(parentBounds: IntRect, menuBounds: IntRect): TransformOrigin {
    val pivotX =
        when {
            menuBounds.left >= parentBounds.right -> 0f
            menuBounds.right <= parentBounds.left -> 1f
            menuBounds.width == 0 -> 0f
            else -> {
                val intersectionCenter =
                    (max(parentBounds.left, menuBounds.left) +
                            min(parentBounds.right, menuBounds.right)) / 2
                (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
            }
        }
    val pivotY =
        when {
            menuBounds.top >= parentBounds.bottom -> 0f
            menuBounds.bottom <= parentBounds.top -> 1f
            menuBounds.height == 0 -> 0f
            else -> {
                val intersectionCenter =
                    (max(parentBounds.top, menuBounds.top) +
                            min(parentBounds.bottom, menuBounds.bottom)) / 2
                (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
            }
        }
    return TransformOrigin(pivotX, pivotY)
}

@Composable
internal fun ExposedDropdownMenuPopup(
    onDismissRequest: (() -> Unit)?,
    popupPositionProvider: PopupPositionProvider,
    content: @Composable () -> Unit
) {
    var focusManager: FocusManager? by mutableStateOf(null)
    var inputModeManager: InputModeManager? by mutableStateOf(null)
    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        focusManager = LocalFocusManager.current
        inputModeManager = LocalInputModeManager.current
        content()
    }
}

private const val InTransitionDuration = 120
private const val OutTransitionDuration = 75

@Composable
internal fun DropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
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
        Column(
            modifier =
                modifier
                    .padding(vertical = Spacing.sm)
                    .width(IntrinsicSize.Max)
                    .verticalScroll(scrollState),
            content = content
        )
    }
}

@Composable
fun SearchDropdownMenuItem(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        text = text,
        onClick = {},
        modifier = modifier.clickable(enabled = false, onClick = { }),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

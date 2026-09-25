package fr.vinarnt.animu.finder.compose.ui.component.navigation.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.*

@Composable
fun NavigationBar(
    title: String? = null,
    brand: String? = null,
    overlay: Boolean = false,
    scrolled: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow
    val onImage = overlay && !scrolled

    val containerColor = when {
        !overlay -> MaterialTheme.colorScheme.surfaceContainerLowest
        scrolled -> appBarGlassColor()
        else -> Color.Transparent
    }
    val contentColor = when {
        onImage -> onScrim
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dividerColor = if (overlay && !scrolled) Color.Transparent
    else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Size.AppBar.height)
            .background(containerColor)
            .drawBehind {
                if (dividerColor != Color.Transparent) {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, size.height - 1f),
                        end = Offset(size.width, size.height - 1f),
                        strokeWidth = 1f,
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (navigator.canPop) {
                    NavIconButton(
                        onClick = { navigator.pop() },
                        onImage = onImage,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.navigation.back)
                    }
                    // Show home shortcut when at least two screens deep (home -> x -> y).
                    if (navigator.size >= 3) {
                        NavIconButton(
                            onClick = { navigator.popUntilRoot() },
                            onImage = onImage,
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = strings.navigation.home)
                        }
                    }
                }

                val text = title ?: brand
                if (text != null) {
                    Text(
                        text = text,
                        style = if (brand != null) BrandTypography else AppBarTypography,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavIconButton(
    onClick: () -> Unit,
    onImage: Boolean,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(Size.TouchTarget.min)
            .pointerHoverIcon(PointerIcon.Hand)
            .then(
                if (onImage) {
                    Modifier
                        .clip(CircleShape)
                        .background(heroGlass)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
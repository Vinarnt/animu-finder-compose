package fr.vinarnt.animu.finder.compose.ui.component.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.AnimeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.theme.*
import kotlin.math.round

@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    malId: Int,
    title: String,
    thumbnailUrl: String,
    score: Double? = null,
    badgeText: String? = null,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val navigator = LocalNavigator.currentOrThrow
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val lift by animateFloatAsState(
        targetValue = if (isHovered) -3f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "cardLift"
    )
    val zoom by animateFloatAsState(
        targetValue = if (isHovered) 1.07f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "cardZoom"
    )

    Surface(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .graphicsLayer {
                translationY = lift
            }
            .shadow(
                elevation = if (isHovered) Elevation.lg else 0.dp,
                shape = RoundedCornerShape(CornerRadius.md),
                clip = false,
            )
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable {
                if (onClick != null) onClick() else navigator.push(AnimeDetailScreen(malId))
            },
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                    },
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { Logger.e("Error loading AnimeCard image: ${it.result.throwable}") }
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(CornerRadius.xs)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (score != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(Color(0, 0, 0, 184), RoundedCornerShape(CornerRadius.xs))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = scoreGold,
                        modifier = Modifier.size(Size.Icon.sm)
                    )
                    val roundedScore = round(score * 100) / 100
                    val scoreText =
                        if (roundedScore % 1.0 == 0.0) roundedScore.toInt().toString() else roundedScore.toString()
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.labelSmall,
                        color = onScrim,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.3f to Color(0, 0, 0, 56),
                            0.62f to Color(0, 0, 0, 173),
                            1f to Color(0, 0, 0, 235),
                        )
                    )
                    .padding(start = 10.dp, end = 10.dp, top = 44.dp, bottom = 10.dp)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = onScrim,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = onScrim.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
package fr.vinarnt.animu.finder.compose.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults.outlinedCardBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import kotlin.math.round
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.AnimeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.scoreGold

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

    OutlinedCard(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .hoverable(interactionSource = interactionSource)
            .clickable {
                if (onClick != null) onClick() else navigator.push(AnimeDetailScreen(malId))
            },
        border = outlinedCardBorder(isHovered).copy(3.dp),
    ) {
        Box {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { Logger.e("Error loading AnimeCard image: ${it.result.throwable}") }
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.sm)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(CornerRadius.sm)
                        )
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else if (score != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.sm)
                        .background(Color(0, 0, 0, 180), RoundedCornerShape(CornerRadius.sm))
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = scoreGold,
                        modifier = Modifier.size(Size.Icon.sm)
                    )
                    val roundedScore = round(score * 100) / 100
                    val scoreText = if (roundedScore % 1.0 == 0.0) roundedScore.toInt().toString() else roundedScore.toString()
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier
                    .background(Color(0, 0, 0, 200))
                    .fillMaxWidth()
                    .padding(Spacing.sm)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 5
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

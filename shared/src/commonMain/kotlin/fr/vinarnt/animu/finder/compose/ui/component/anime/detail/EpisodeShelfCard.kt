package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

private val CardShape = RoundedCornerShape(10.dp)
private val BadgeShape = RoundedCornerShape(6.dp)

/**
 * Episode card for the episode shelf.
 */
@Composable
fun EpisodeShelfCard(
    number: Int,
    numberLabel: String,
    title: String,
    subtitle: String?,
    nowPlayingLabel: String,
    imageUrl: String?,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(Size.Card.landscapeWidth)
            .aspectRatio(16f / 9f)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(BadgeShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        ) {
            Text(
                text = numberLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.xs)
                    .clip(BadgeShape)
                    .background(Color(0xB8000000))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            ) {
                Text(
                    text = nowPlayingLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to Color(0xCC000000),
                    )
                )
                .padding(start = 10.dp, end = 10.dp, top = 30.dp, bottom = 8.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, MaterialTheme.colorScheme.primary, CardShape)
            )
        }
    }
}

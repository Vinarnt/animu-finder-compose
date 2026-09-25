package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim
import fr.vinarnt.animu.finder.compose.util.platformImageUrl
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData

@Composable
fun UpNextCard(
    nextEpisode: GetAnimeByIdEpisodesByEpisodeId200ResponseData?,
    nextEpisodeNumber: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val s = strings.episodeDetail
    val title = nextEpisode?.title ?: s.episodeTitleFallback(nextEpisodeNumber)
    val imageUrl = platformImageUrl(nextEpisode?.images?.jpg?.imageUrl)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(CornerRadius.md))
                .then(
                    if (onClick != null) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(CornerRadius.md))
                    } else {
                        Modifier
                    }
                )
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }

            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(CornerRadius.xs))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            ) {
                Text(
                    text = s.nextEpisode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to Color(0, 0, 0, 220),
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = s.upNext.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onScrim.copy(alpha = 0.8f),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = onScrim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.scoreGold
import fr.vinarnt.animu.finder.compose.util.platformImageUrl
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import kotlin.math.roundToInt

/** 16:9 episode thumbnail with a "#N" badge and a score overlay. */
@Composable
internal fun EpisodeGridThumbnail(
    episode: GetAnimeByIdEpisodes200ResponseDataInner,
    fallbackImageUrl: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.TopStart
    ) {
        val imageUrl = episode.images?.jpg?.imageUrl?.let { platformImageUrl(it) }
            ?: fallbackImageUrl
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .padding(Spacing.xs)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(CornerRadius.sm)
                )
        ) {
            Text(
                text = "#${episode.malId ?: "?"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
            )
        }
        episode.score?.let { score ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.xs)
                    .background(
                        color = Color(0, 0, 0, 184),
                        shape = RoundedCornerShape(CornerRadius.sm)
                    )
            ) {
                Text(
                    text = "★ ${(score * 100).roundToInt() / 100.0}".trimEnd('0').trimEnd('.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = scoreGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                )
            }
        }
    }
}

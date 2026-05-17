package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.Anime

@Composable
fun AnimeDetailLayoutStaticHeader(
    anime: Anime,
    isExpanded: Boolean
) {
    val imageSize by animateDpAsState(if (isExpanded) Size.Image.md else Size.Image.sm)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        AsyncImage(
            model = anime.images?.let { it.webp?.imageUrl ?: it.jpg?.imageUrl },
            contentDescription = null,
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(CornerRadius.sm)),
            contentScale = ContentScale.Crop,
            onError = { Logger.e("Error loading anime image: ${it.result.throwable}") }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getTitle(anime),
                style = MaterialTheme.typography.titleLarge
            )
            AnimeDetailSubtitle(
                anime.studios?.mapNotNull { it.name }?.takeIf { it.isNotEmpty() },
                anime.aired?.prop?.from?.year,
                anime.status
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AnimeDetailMetrics(anime)
            }
        }
    }

}

private fun getTitle(anime: Anime) =
    (anime.titles?.first { it.type in listOf("English", "Default") }?.title
        ?: anime.titles?.get(0)?.title
        ?: "Unknown")

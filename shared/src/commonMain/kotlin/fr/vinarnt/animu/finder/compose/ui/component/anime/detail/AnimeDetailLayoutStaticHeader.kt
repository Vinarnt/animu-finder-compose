package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Elevation
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun AnimeDetailLayoutStaticHeader(
    anime: GetAnimeById200ResponseData,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val posterHeight by animateDpAsState(if (isExpanded) 150.dp else 96.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = Elevation.sm,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            AsyncImage(
                model = anime.images?.let { it.webp?.largeImageUrl ?: it.jpg?.largeImageUrl },
                contentDescription = null,
                modifier = Modifier
                    .height(posterHeight)
                    .width(posterHeight * 2f / 3f)
                    .clip(RoundedCornerShape(CornerRadius.sm)),
                contentScale = ContentScale.Crop,
                onError = { Logger.e("Error loading anime image: ${it.result.throwable}") }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = getTitle(anime),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AnimeDetailSubtitle(
                    studios = anime.studios?.mapNotNull { it.name }?.takeIf { it.isNotEmpty() },
                    airedYear = anime.aired?.prop?.from?.year,
                    status = anime.status
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        AnimeDetailMetrics(anime)
                        GenreChips(anime.genres)
                    }
                }
            }
        }
    }
}

private fun getTitle(anime: GetAnimeById200ResponseData) =
    (anime.titles?.firstOrNull { it.type in listOf("English", "Default") }?.title
        ?: anime.titles?.getOrNull(0)?.title
        ?: "Unknown")

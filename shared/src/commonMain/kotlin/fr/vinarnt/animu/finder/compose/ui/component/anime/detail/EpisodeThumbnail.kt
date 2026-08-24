package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner

@Composable
fun EpisodeThumbnail(
    episode: GetAnimeByIdEpisodes200ResponseDataInner,
    modifier: Modifier,
    placeholderTextStyle: TextStyle,
) {
    val imageUrl = episode.images?.jpg?.imageUrl
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(CornerRadius.sm)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "${episode.malId ?: "?"}",
                style = placeholderTextStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

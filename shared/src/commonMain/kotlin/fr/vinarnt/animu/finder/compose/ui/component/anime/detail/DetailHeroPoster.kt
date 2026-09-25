package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.ui.theme.Size

/** The detail banner's portrait poster, sized from the [Size.Image] tokens. */
@Composable
internal fun DetailHeroPoster(posterUrl: String?, compact: Boolean) {
    val posterWidth = if (compact) Size.Image.heroPosterWidthCompact else Size.Image.heroPosterWidth
    val posterHeight = if (compact) Size.Image.heroPosterHeightCompact else Size.Image.heroPosterHeight
    val shape = RoundedCornerShape(if (compact) 10.dp else 14.dp)

    Box(
        modifier = Modifier
            .width(posterWidth)
            .height(posterHeight)
            .shadow(elevation = 16.dp, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { Logger.e("Error loading hero poster: ${it.result.throwable}") }
            )
        }
    }
}

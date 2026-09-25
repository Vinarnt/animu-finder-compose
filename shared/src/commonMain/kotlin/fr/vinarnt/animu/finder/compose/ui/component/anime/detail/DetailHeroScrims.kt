package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** The detail banner's left shade and bottom fade-to-background scrims. */
@Composable
internal fun DetailHeroScrims() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    0f to Color(0xB307090D),
                    0.46f to Color(0x6107090D),
                    0.78f to Color.Transparent,
                    1f to Color.Transparent,
                )
            )
    )

    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.4f to Color.Transparent,
                    0.74f to background.copy(alpha = 0.72f),
                    1f to background,
                )
            )
    )
}

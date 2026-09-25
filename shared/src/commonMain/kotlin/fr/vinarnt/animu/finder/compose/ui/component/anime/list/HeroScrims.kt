package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** The two hero scrims: a left-to-right one for copy legibility and a bottom shade for the CTA row. */
@Composable
internal fun HeroScrims() {
    // Left-to-right scrim keeps copy readable.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    0f to Color(0xF207090D),
                    0.4f to Color(0xB307090D),
                    0.7f to Color(0x3D07090D),
                    1f to Color(0x0F07090D),
                )
            )
    )

    // Bottom shade for the CTA row.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to Color(0xE607090D),
                )
            )
    )
}

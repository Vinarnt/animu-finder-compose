package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlass
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlassBorder
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

/** Up to five glass genre pills under the banner meta line (hidden on compact layouts). */
@Composable
internal fun HeroChips(anime: GetAnimeById200ResponseData) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        anime.genres.orEmpty().take(5).forEach { genre ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(heroGlass)
                    .border(1.dp, heroGlassBorder, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 11.dp, vertical = 5.dp)
            ) {
                Text(
                    text = genre.name ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = onScrim,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

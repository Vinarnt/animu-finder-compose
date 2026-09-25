package fr.vinarnt.animu.finder.compose.ui.component.anime.list

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
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlass
import fr.vinarnt.animu.finder.compose.ui.theme.heroGlassBorder
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/** Up to four glass genre pills under the hero meta line (hidden on compact layouts). */
@Composable
internal fun HeroGenreChips(anime: GetAnime200ResponseDataInner) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        anime.genres.orEmpty().take(4).forEach { genre ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(heroGlass)
                    .border(1.dp, heroGlassBorder, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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

package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.scoreGold
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner
import kotlin.math.round

/** Score · year · type meta line under the hero title. */
@Composable
internal fun HeroMetaLine(anime: GetAnime200ResponseDataInner) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        anime.score?.let { score ->
            Text(
                text = "★ ${round(score * 10) / 10}",
                style = MaterialTheme.typography.labelLarge,
                color = scoreGold,
                fontWeight = FontWeight.Bold
            )
            Text("·", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
        }
        anime.year?.let { year ->
            Text("$year", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f))
            Text("·", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
        }
        anime.type?.let { type ->
            Text(type, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f))
        }
    }
}

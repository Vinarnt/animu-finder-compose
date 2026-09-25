package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

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
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import kotlin.math.round

/** Score · year · type · episodes · status meta line under the banner title. */
@Composable
internal fun HeroMeta(anime: GetAnimeById200ResponseData) {
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
        anime.aired?.prop?.from?.year?.let { year ->
            Text("$year", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f))
            Text("·", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
        }
        anime.type?.let { type ->
            Text(type, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f))
        }
        anime.episodes?.let { eps ->
            Text("·", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
            Text("$eps eps", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.92f))
        }
        anime.status?.let { status ->
            Text("·", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
            Text(
                text = status.replace(" Airing", "").replace(" aired", ""),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

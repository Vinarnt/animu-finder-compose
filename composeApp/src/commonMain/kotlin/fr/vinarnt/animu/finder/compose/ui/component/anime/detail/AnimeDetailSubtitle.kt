package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.base.Tooltip
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun AnimeDetailSubtitle(
    studios: List<String>?,
    airedYear: Int?,
    status: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        ) {
            Tooltip({ Text(strings.animeDetail.studiosDescription) }) {
                Text(
                    text = studios?.joinToString(", ") ?: "N/A",
                )
            }
            Text("·", fontWeight = FontWeight.Bold)
            Tooltip({ Text(strings.animeDetail.airedDescription) }) {
                Text(text = airedYear?.toString() ?: "N/A")
            }
            Text("·", fontWeight = FontWeight.Bold)
            Tooltip({ Text(strings.animeDetail.statusDescription) }) {
                AnimeStatusItem(status)
            }
        }
    }
}
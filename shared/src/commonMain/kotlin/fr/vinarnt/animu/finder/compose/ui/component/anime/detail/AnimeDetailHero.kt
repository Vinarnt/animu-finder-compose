package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.theme.Breakpoints
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

/**
 * Anime detail banner.
 */
@Composable
fun AnimeDetailHero(
    anime: GetAnimeById200ResponseData,
    onWatch: (() -> Unit)? = null,
    watchLabel: String? = null,
    modifier: Modifier = Modifier,
    inMyList: Boolean = false,
    onToggleMyList: (() -> Unit)? = null,
) {
    val posterUrl = anime.images?.let { it.webp?.largeImageUrl ?: it.jpg?.largeImageUrl }
    val backdropUrl = anime.trailer?.images?.maximumImageUrl
        ?: anime.trailer?.images?.largeImageUrl
        ?: posterUrl

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val compact = maxWidth < Breakpoints.compactMaxWidth
        val heroHeight = if (compact) Size.Hero.heightCompact else Size.Hero.height
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        val gutter = if (compact) 16.dp else 24.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        ) {
            DetailHeroBackdrop(backdropUrl)
            DetailHeroScrims()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = sidePadding + gutter,
                        end = sidePadding + gutter,
                        top = Size.AppBar.height,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 24.dp)
            ) {
                DetailHeroPoster(posterUrl = posterUrl, compact = compact)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else Spacing.sm)
                ) {
                    Text(
                        text = resolveAnimeTitle(anime, strings.common.unknown),
                        style = MaterialTheme.typography.headlineLarge,
                        color = onScrim,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    HeroMeta(anime)

                    if (!compact) {
                        HeroChips(anime)
                    }

                    DetailHeroActions(
                        onWatch = onWatch,
                        watchLabel = watchLabel,
                        inMyList = inMyList,
                        onToggleMyList = onToggleMyList,
                        compact = compact,
                    )
                }
            }
        }
    }
}

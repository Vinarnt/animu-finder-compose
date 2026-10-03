package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.AddToMyListButton
import fr.vinarnt.animu.finder.compose.ui.component.HeroCtaButton
import fr.vinarnt.animu.finder.compose.ui.theme.WindowSizeClass
import fr.vinarnt.animu.finder.compose.ui.theme.windowSizeClass
import fr.vinarnt.animu.finder.compose.ui.theme.HeroKickerTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.ui.theme.accentHero
import fr.vinarnt.animu.finder.compose.ui.theme.onScrim
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/**
 * Featured hero for the home page.
 */
@Composable
fun HomeHero(
    anime: GetAnime200ResponseDataInner,
    index: Int,
    count: Int,
    progress: Float,
    onPlay: () -> Unit,
    onDotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    inMyList: Boolean = false,
    onToggleMyList: (() -> Unit)? = null,
) {
    val backdropUrl = anime.trailer?.images?.maximumImageUrl
        ?: anime.trailer?.images?.largeImageUrl
        ?: anime.images?.webp?.largeImageUrl
        ?: anime.images?.jpg?.imageUrl
    val posterUrl = anime.images?.webp?.largeImageUrl ?: anime.images?.jpg?.imageUrl

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = windowSizeClass().isAtMost(WindowSizeClass.Medium)
        val tablet = windowSizeClass() == WindowSizeClass.Expanded
        val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f).coerceAtLeast(0.dp)
        val hPad = if (compact) 16.dp else 24.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 320.dp else if (tablet) 360.dp else 400.dp)
        ) {
            HeroBackdrop(backdropUrl)
            HeroScrims()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = sidePadding + hPad,
                        end = sidePadding + hPad,
                        top = if (compact) 52.dp else 56.dp,
                        bottom = if (compact) 24.dp else if (tablet) 28.dp else 30.dp
                    ),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 32.dp)
            ) {
                if (!compact) {
                    HeroPoster(posterUrl)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else 12.dp)
                ) {
                    Text(
                        text = strings.home.featured,
                        style = HeroKickerTypography,
                        color = accentHero,
                    )
                    Text(
                        text = anime.title ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        color = onScrim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    HeroMetaLine(anime)
                    if (!compact) {
                        HeroGenreChips(anime)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (compact) Spacing.xs else Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HeroCtaButton(onClick = onPlay, compact = compact)
                        if (onToggleMyList != null) {
                            AddToMyListButton(
                                inList = inMyList,
                                compact = compact,
                                onClick = onToggleMyList,
                            )
                        }
                    }
                }
            }

            HeroDots(
                index = index,
                count = count,
                onDotClick = onDotClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    // Sits just below the app bar, which is also the window's title bar on desktop.
                    .padding(top = if (compact) 60.dp else 64.dp, end = hPad),
            )

            HeroProgressBar(
                progress = progress,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

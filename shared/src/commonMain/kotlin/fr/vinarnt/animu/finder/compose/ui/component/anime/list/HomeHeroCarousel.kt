package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner

/**
 * Auto-advancing carousel for the home hero. Drives [HomeHero] with a timed
 * progress bar, cycling through the first five [items].
 */
@Composable
internal fun HomeHeroCarousel(
    items: List<GetAnime200ResponseDataInner>,
    onPlay: (GetAnime200ResponseDataInner) -> Unit,
    myList: List<Int>,
    onToggleMyList: (GetAnime200ResponseDataInner) -> Unit,
) {
    val heroItems = items.take(5)
    var index by remember { mutableStateOf(0) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(heroItems, index) {
        if (heroItems.size <= 1) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 5000, easing = LinearEasing))
        index = (index + 1) % heroItems.size
    }

    val current = heroItems[index % heroItems.size]
    HomeHero(
        anime = current,
        index = index,
        count = heroItems.size,
        progress = progress.value,
        onPlay = { onPlay(current) },
        onDotClick = { dotIndex ->
            if (dotIndex != index) {
                index = dotIndex
            }
        },
        inMyList = current.malId in myList,
        onToggleMyList = { onToggleMyList(current) },
    )
}

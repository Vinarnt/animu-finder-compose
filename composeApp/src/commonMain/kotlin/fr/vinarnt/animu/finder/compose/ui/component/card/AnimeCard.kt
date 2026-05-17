package fr.vinarnt.animu.finder.compose.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults.outlinedCardBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import fr.vinarnt.animu.finder.compose.navigation.screen.anime.detail.AnimeDetailScreen
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing

@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    malId: Int,
    title: String,
    thumbnailUrl: String,
) {
    val navigator = LocalNavigator.currentOrThrow
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    OutlinedCard(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .hoverable(interactionSource = interactionSource)
            .clickable {
                navigator.push(AnimeDetailScreen(malId))
            },
        border = outlinedCardBorder(isHovered).copy(3.dp),
    ) {
        Box {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { Logger.e("Error loading AnimeCard image: ${it.result.throwable}") }
            )
            Column(
                modifier = Modifier
                    .background(Color(0, 0, 0, 200))
                    .fillMaxWidth()
                    .padding(Spacing.sm)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 5
                )
            }
        }
    }
}

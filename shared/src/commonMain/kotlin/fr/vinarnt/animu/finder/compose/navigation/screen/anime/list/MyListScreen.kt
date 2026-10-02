package fr.vinarnt.animu.finder.compose.navigation.screen.anime.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.ui.component.BookmarkIcon
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.card.AnimeCard
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.theme.SectionTitleTypography
import fr.vinarnt.animu.finder.compose.ui.theme.Size
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import fr.vinarnt.animu.finder.compose.viewmodel.MyListViewModel
import org.koin.compose.viewmodel.koinViewModel

class MyListScreen : Screen {

    override val key: ScreenKey = "anime:mylist"

    @Composable
    override fun Content() {
        val vm: MyListViewModel = koinViewModel()
        val items by vm.items.collectAsStateWithLifecycle()
        val loading by vm.loading.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow
        val gridState = rememberLazyGridState()
        val myListStrings = strings.myList
        val unknownFallback = strings.common.unknown

        MainLayout(
            topBar = {
                NavigationBar(title = myListStrings.title)
            },
            scrollable = false
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val sidePadding = ((maxWidth - Size.maxContentWidth) / 2f)
                    .coerceAtLeast(0.dp)
                when {
                    loading && items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(Size.CircleProgressIndicator.md))
                        }
                    }

                    items.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    BookmarkIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = myListStrings.emptyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = myListStrings.emptyHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 48.dp),
                            )
                            Button(
                                onClick = { navigator.push(BrowseScreen()) },
                                modifier = Modifier.padding(top = Spacing.sm),
                            ) {
                                Text(myListStrings.browseAnime)
                            }
                        }
                    }

                    else -> {
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Adaptive(150.dp),
                            state = gridState,
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            contentPadding = PaddingValues(
                                start = sidePadding + Spacing.md,
                                end = sidePadding + Spacing.md,
                                top = Spacing.md,
                                bottom = Spacing.md
                            ),
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "${myListStrings.title} (${items.size})",
                                    style = SectionTitleTypography,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            items(items, key = { "mylist:${it.malId ?: 0}" }) { anime ->
                                AnimeCard(
                                    title = anime.titles?.firstOrNull()?.title ?: unknownFallback,
                                    malId = anime.malId ?: 0,
                                    thumbnailUrl = anime.images?.jpg?.imageUrl
                                        ?: anime.images?.webp?.imageUrl ?: "",
                                    score = anime.score,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
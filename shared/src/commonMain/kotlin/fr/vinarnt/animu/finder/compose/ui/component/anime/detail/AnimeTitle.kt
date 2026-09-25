package fr.vinarnt.animu.finder.compose.ui.component.anime.detail

import androidx.compose.runtime.Composable
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

@Composable
fun resolveAnimeTitle(anime: GetAnimeById200ResponseData): String =
    resolveAnimeTitle(anime, strings.common.unknown)

/** Resolves the display title, falling back to [unknownFallback] when the API has no title. */
fun resolveAnimeTitle(anime: GetAnimeById200ResponseData, unknownFallback: String): String =
    anime.titles?.firstOrNull { it.type in listOf("English", "Default") }?.title
        ?: anime.titles?.firstOrNull()?.title
        ?: unknownFallback

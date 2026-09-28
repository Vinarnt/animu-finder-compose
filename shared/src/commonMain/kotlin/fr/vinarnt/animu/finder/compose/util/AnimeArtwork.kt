package fr.vinarnt.animu.finder.compose.util

import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData

/**
 * The title's widest available artwork (trailer backdrop first, then poster).
 *
 * Used as an episode-thumbnail fallback: the API has no per-episode images for some
 * titles (e.g. simulcasts like Code Geass: Rozé of the Recapture), so episode lists
 * would otherwise show empty placeholders.
 */
fun animeArtworkUrl(anime: GetAnimeById200ResponseData): String? =
    anime.trailer?.images?.maximumImageUrl
        ?: anime.trailer?.images?.largeImageUrl
        ?: anime.images?.webp?.largeImageUrl
        ?: anime.images?.jpg?.largeImageUrl
        ?: anime.images?.jpg?.imageUrl

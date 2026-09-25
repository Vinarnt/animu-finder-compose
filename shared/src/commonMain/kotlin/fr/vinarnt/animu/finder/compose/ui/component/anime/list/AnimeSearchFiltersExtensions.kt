package fr.vinarnt.animu.finder.compose.ui.component.anime.list

import fr.vinarnt.animu.finder.compose.viewmodel.AnimeSearchFilters

/** True when any browse filter differs from its default. */
internal fun AnimeSearchFilters.isActive(): Boolean =
    queryText.isNotBlank() ||
        type != null ||
        status != null ||
        rating != null ||
        genres.isNotEmpty() ||
        scoreRange != 1f..10f

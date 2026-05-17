package fr.vinarnt.animu.finder.compose.i18n

import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.jikan4k.models.AnimeTypes

data class Strings(
    val navigation: NavigationStrings,
    val ui: UIStrings,
    val settings: SettingStrings,
    val languages: LanguagesStrings,
    val animeList: AnimeListStrings,
    val animeDetail: AnimeDetailStrings,
)

data class NavigationStrings(
    val back: String
)

data class SettingStrings(
    val title: String,
    val theme: SettingThemeStrings,
    val locale: SettingLocaleStrings
)

data class SettingThemeStrings(
    val label: String,
    val values: SettingThemeValuesStrings
)

data class SettingLocaleStrings(
    val label: String
)

data class SettingThemeValuesStrings(
    val system: String,
    val light: String,
    val dark: String
)

data class LanguagesStrings(
    val locales: List<String>,
    val localeLabels: Map<String, String>,
    val getLocaleLabel: (locale: String) -> String
)

data class UIStrings(
    val dropdown: DropdownStrings
)

data class DropdownStrings(
    val search: String,
    val noContent: String
)

data class AnimeDetailStrings(
    val episodesDescription: String,
    val scoreDescription: String,
    val rankDescription: String,
    val popularityDescription: String,
    val studiosDescription: String,
    val airedDescription: String,
    val statusDescription: String,
    val synopsisLabel: String
)

data class AnimeListStrings(
    val searchPlaceholder: String,
    val typeLabel: String,
    val statusLabel: String,
    val ratingLabel: String,
    val scoreLabel: String,
    val all: String,
    val typeLabels: Map<AnimeTypes, String>,
    val statusLabels: Map<AnimeSearchQueryStatus, String>,
    val ratingLabels: Map<AnimeSearchQueryRating, String>,
    val genreLabel: String,
    val genreCount: (Int) -> String,
    val genreNames: Map<AnimeGenre, String>
)

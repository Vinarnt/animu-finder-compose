package fr.vinarnt.animu.finder.compose.i18n

import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.apis.AnimeApi

data class Strings(
    val navigation: NavigationStrings,
    val home: HomeStrings,
    val ui: UIStrings,
    val settings: SettingStrings,
    val languages: LanguagesStrings,
    val animeList: AnimeListStrings,
    val animeDetail: AnimeDetailStrings,
    val episodeDetail: EpisodeDetailStrings,
)

data class NavigationStrings(
    val back: String
)

data class HomeStrings(
    val continueWatching: String,
    val resume: String
)

data class SettingStrings(
    val title: String,
    val theme: SettingThemeStrings,
    val locale: SettingLocaleStrings,
    val appearance: String,
    val playback: String,
    val defaultQuality: String,
    val preferredSubtitles: String,
    val autoplayNext: String
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
    val membersDescription: String,
    val studiosDescription: String,
    val airedDescription: String,
    val statusDescription: String,
    val synopsisLabel: String,
    val episodesLabel: String
)

data class EpisodeDetailStrings(
    val alternativeTitles: String,
    val airingDate: String,
    val synopsis: String,
    val streams: String,
    val loadingStreams: String,
    val noStreams: String,
    val couldNotLoadStreams: String,
    val noSource: String,
    val dub: String,
    val sub: String,
    val upNext: String,
    val nextEpisode: String,
    val filler: String,
    val recap: String,
    val score: String,
    val episodeNumber: String
)

data class AnimeListStrings(
    val searchPlaceholder: String,
    val typeLabel: String,
    val statusLabel: String,
    val ratingLabel: String,
    val scoreLabel: String,
    val all: String,
    val typeLabels: Map<AnimeApi.TypeGetAnime, String>,
    val statusLabels: Map<AnimeApi.StatusGetAnime, String>,
    val ratingLabels: Map<AnimeApi.RatingGetAnime, String>,
    val genreLabel: String,
    val genreCount: (Int) -> String,
    val genreNames: Map<AnimeGenre, String>
)

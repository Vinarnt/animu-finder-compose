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
    val player: PlayerStrings,
    val myList: MyListStrings,
    val common: CommonStrings,
)

data class NavigationStrings(
    val back: String,
    val home: String,
    val appName: String
)

data class HomeStrings(
    val continueWatching: String,
    val episodeBadge: (Int) -> String,
    val featured: String,
    val trending: String,
    val newEpisodes: String,
    val topRated: String,
    val action: String,
    val fantasy: String,
    val browsePanelSubtitle: String,
    val browsePanelOpen: String
)

/** Shared chrome copy reused across screens. */
data class CommonStrings(
    val unknown: String,
    val retry: String,
    val seeAll: String,
    val scrollLeft: String,
    val scrollRight: String,
    val synopsis: String,
    val score: String
)

data class MyListStrings(
    val title: String,
    val emptyTitle: String,
    val emptyHint: String,
    val browseAnime: String,
    val addLabel: String,
    val addedLabel: String
)

data class SettingStrings(
    val title: String,
    val theme: SettingThemeStrings,
    val locale: SettingLocaleStrings,
    val appearance: String,
    val general: String,
    val playback: String,
    val debug: String,
    val defaultQuality: String,
    val preferredSubtitles: String,
    val autoplayNext: String,
    val qualityAuto: String,
    val subtitleLanguageEnglish: String,
    val subtitleLanguageFrench: String,
    val subtitleLanguageJapanese: String,
    val subtitleLanguageNone: String,
    val cloudflare: SettingCloudflareStrings
)

/**
 * Copy for the Cloudflare clearance capture (shared by all providers).
 */
data class SettingCloudflareStrings(
    val label: String,
    val description: String,
    val openSite: String,
    val save: String,
    val clear: String,
    val statusConfigured: String,
    val statusMissing: String,
    val captureHint: String,
    val captureButton: String,
    val captureFailed: String,
    val captureSuccess: String,
    val captureUnsupported: String,
    val solving: String,
    val cancel: String,
    val cookieLabel: String,
    val cookieHint: String,
    val userAgentLabel: String,
    val userAgentHint: String
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
    val studiosDescription: String,
    val airedDescription: String,
    val statusDescription: String,
    val episodesLabel: String
)

data class EpisodeDetailStrings(
    val alternativeTitles: String,
    val airingDate: String,
    val streams: String,
    val loadingStreams: String,
    val noStreams: String,
    val noSource: String,
    val dub: String,
    val sub: String,
    val upNext: String,
    val nextEpisode: String,
    val nowPlaying: String,
    val filler: String,
    val recap: String,
    val episodeNumber: String,
    val episodeShort: String,
    val episodeTitleFallback: (Int) -> String
)

data class PlayerStrings(
    val play: String,
    val pause: String,
    val fullscreen: String,
    val exitFullscreen: String,
    val audio: String,
    val error: String
)

data class AnimeListStrings(
    val searchPlaceholder: String,
    val typeLabel: String,
    val statusLabel: String,
    val ratingLabel: String,
    val all: String,
    val typeLabels: Map<AnimeApi.TypeGetAnime, String>,
    val statusLabels: Map<AnimeApi.StatusGetAnime, String>,
    val ratingLabels: Map<AnimeApi.RatingGetAnime, String>,
    val genreLabel: String,
    val genreCount: (Int) -> String,
    val genreNames: Map<AnimeGenre, String>,
    val browseTitle: String,
    val clearFilters: String,
    val emptyTitle: String,
    val emptyHint: String,
    val resultCount: (Int) -> String,
    val sortLabel: String,
    val sortNewest: String,
    val sortOldest: String,
    val sortAlphabetical: String
)

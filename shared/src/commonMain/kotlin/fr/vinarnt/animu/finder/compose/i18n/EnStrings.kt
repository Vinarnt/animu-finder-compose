package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.apis.AnimeApi

private val languages = mapOf(
    Locales.EN to "English", Locales.FR to "French"
)

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnStrings = Strings(
    navigation = NavigationStrings(
        back = "Back",
        home = "Home",
        appName = "Animu Finder"
    ), home = HomeStrings(
        continueWatching = "Continue watching",
        episodeBadge = { n -> "EP $n" },
        featured = "Featured",
        trending = "Trending now",
        newEpisodes = "New episodes",
        topRated = "Top rated",
        action = "Action",
        fantasy = "Fantasy",
        browsePanelSubtitle = "Search and filter by type, status, genre and score",
        browsePanelOpen = "Open ›"
    ), common = CommonStrings(
        unknown = "Unknown",
        retry = "Retry",
        seeAll = "See all ›",
        scrollLeft = "Scroll left",
        scrollRight = "Scroll right",
        synopsis = "Synopsis",
        score = "Score"
    ), myList = MyListStrings(
        title = "My List",
        emptyTitle = "Your list is empty",
        emptyHint = "Open any title and tap \"Add to My List\" to save it here.",
        browseAnime = "Browse anime",
        addLabel = "Add to My List",
        addedLabel = "Added to My List"
    ), ui = UIStrings(
        dropdown = DropdownStrings(
            search = "Search", noContent = "No item matching"
        )
    ), settings = SettingStrings(
        title = "Settings", theme = SettingThemeStrings(
            label = "Theme", values = SettingThemeValuesStrings(
                system = "System", light = "Light", dark = "Dark"
            )
        ), locale = SettingLocaleStrings(
            label = "Language"
        ),
        appearance = "Appearance",
        general = "General",
        playback = "Playback",
        debug = "Debug",
        defaultQuality = "Default quality",
        preferredSubtitles = "Preferred subtitles",
        autoplayNext = "Autoplay next episode",
        qualityAuto = "Auto",
        subtitleLanguageEnglish = "English",
        subtitleLanguageFrench = "French",
        subtitleLanguageJapanese = "Japanese",
        subtitleLanguageNone = "None",
        cloudflare = SettingCloudflareStrings(
            label = "Cloudflare",
            description = "Some providers are protected by Cloudflare. Solve the check once and Animu Finder reuses the cookie, for any provider.",
            openSite = "Open in browser",
            save = "Save",
            clear = "Clear",
            statusConfigured = "Configured",
            statusMissing = "Not configured",
            captureHint = "Complete the Cloudflare check, then tap Capture.",
            captureButton = "Capture",
            captureFailed = "Cookie not found yet. Finish the check first.",
            captureSuccess = "Cookie captured!",
            captureUnsupported = "In-app capture is not available here. Open the site in your browser, solve the check, then paste the cf_clearance cookie below.",
            solving = "Waiting for Cloudflare…",
            cancel = "Cancel",
            cookieLabel = "cf_clearance cookie",
            cookieHint = "Paste the cookie value or the full Cookie header",
            userAgentLabel = "User-Agent",
            userAgentHint = "The User-Agent used when the cookie was created"
        )
    ), languages = LanguagesStrings(
        locales = languages.keys.toList(),
        localeLabels = languages,
        getLocaleLabel = { locale -> languages[locale] ?: locale }), animeList = AnimeListStrings(
        searchPlaceholder = "Search anime...",
        typeLabel = "Type",
        statusLabel = "Status",
        ratingLabel = "Rating",
        all = "All",
        typeLabels = mapOf(
            AnimeApi.TypeGetAnime.TV to "TV",
            AnimeApi.TypeGetAnime.MOVIE to "Movie",
            AnimeApi.TypeGetAnime.OVA to "OVA",
            AnimeApi.TypeGetAnime.SPECIAL to "Special",
            AnimeApi.TypeGetAnime.ONA to "ONA",
            AnimeApi.TypeGetAnime.MUSIC to "Music",
            AnimeApi.TypeGetAnime.CM to "CM",
            AnimeApi.TypeGetAnime.PV to "PV",
            AnimeApi.TypeGetAnime.TV_SPECIAL to "TV Special"
        ),
        statusLabels = mapOf(
            AnimeApi.StatusGetAnime.AIRING to "Airing",
            AnimeApi.StatusGetAnime.COMPLETE to "Finished",
            AnimeApi.StatusGetAnime.UPCOMING to "Upcoming"
        ),
        ratingLabels = mapOf(
            AnimeApi.RatingGetAnime.G to "G: All ages",
            AnimeApi.RatingGetAnime.PG to "PG: Children",
            AnimeApi.RatingGetAnime.PG13 to "PG-13: Teens",
            AnimeApi.RatingGetAnime.R17 to "R-17",
            AnimeApi.RatingGetAnime.R to "R+",
            AnimeApi.RatingGetAnime.RX to "Rx: Hentai"
        ),
        genreLabel = "Genre",
        genreCount = { n -> "$n genres" },
        browseTitle = "Browse all anime",
        clearFilters = "Clear filters",
        emptyTitle = "No anime match",
        emptyHint = "Try a different title or loosen your filters.",
        resultCount = { n -> if (n == 1) "1 result" else "$n results" },
        sortLabel = "Sort",
        sortNewest = "Newest",
        sortOldest = "Oldest",
        sortAlphabetical = "A-Z",
        genreNames = mapOf(
            AnimeGenre.ACTION to "Action",
            AnimeGenre.ADVENTURE to "Adventure",
            AnimeGenre.COMEDY to "Comedy",
            AnimeGenre.AVANT_GARDE to "Avant Garde",
            AnimeGenre.MYSTERY to "Mystery",
            AnimeGenre.DRAMA to "Drama",
            AnimeGenre.ECCHI to "Ecchi",
            AnimeGenre.FANTASY to "Fantasy",
            AnimeGenre.GAME to "Game",
            AnimeGenre.HISTORICAL to "Historical",
            AnimeGenre.HORROR to "Horror",
            AnimeGenre.MARTIAL_ARTS to "Martial Arts",
            AnimeGenre.MECHA to "Mecha",
            AnimeGenre.MUSIC to "Music",
            AnimeGenre.PARODY to "Parody",
            AnimeGenre.ROMANCE to "Romance",
            AnimeGenre.SCHOOL to "School",
            AnimeGenre.SCI_FI to "Sci-Fi",
            AnimeGenre.SPACE to "Space",
            AnimeGenre.SPORTS to "Sports",
            AnimeGenre.SUPER_POWER to "Super Power",
            AnimeGenre.VAMPIRE to "Vampire",
            AnimeGenre.HAREM to "Harem",
            AnimeGenre.SLICE_OF_LIFE to "Slice of Life",
            AnimeGenre.SUPERNATURAL to "Supernatural",
            AnimeGenre.MILITARY to "Military",
            AnimeGenre.POLICE to "Police",
            AnimeGenre.PSYCHOLOGICAL to "Psychological",
            AnimeGenre.SUSPENSE to "Suspense",
            AnimeGenre.AWARD_WINNING to "Award Winning",
            AnimeGenre.GOURMET to "Gourmet"
        )
    ),
    animeDetail = AnimeDetailStrings(
        studiosDescription = "Production studios",
        airedDescription = "Start airing date",
        statusDescription = "Airing status",
        episodesLabel = "Episodes"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Alternative titles",
        airingDate = "Airing date",
        streams = "Available streams",
        loadingStreams = "Loading…",
        noStreams = "No stream found",
        noSource = "No source selected",
        dub = "DUB",
        sub = "SUB",
        upNext = "Up next",
        nextEpisode = "Next episode",
        nowPlaying = "Now playing",
        filler = "Filler",
        recap = "Recap",
        episodeNumber = "Episode {number}",
        episodeShort = "Ep {number}",
        episodeTitleFallback = { number -> "Episode $number" }
    ),
    player = PlayerStrings(
        play = "Play",
        pause = "Pause",
        fullscreen = "Enter fullscreen",
        exitFullscreen = "Exit fullscreen",
        audio = "Audio track",
        error = "Playback error"
    )
)



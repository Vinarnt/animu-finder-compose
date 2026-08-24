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
        back = "Back"
    ), home = HomeStrings(
        continueWatching = "Continue watching",
        resume = "Resume"
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
        playback = "Playback",
        defaultQuality = "Default quality",
        preferredSubtitles = "Preferred subtitles",
        autoplayNext = "Autoplay next episode"
    ), languages = LanguagesStrings(
        locales = languages.keys.toList(),
        localeLabels = languages,
        getLocaleLabel = { locale -> languages[locale] ?: locale }), animeList = AnimeListStrings(
        searchPlaceholder = "Search anime...",
        typeLabel = "Type",
        statusLabel = "Status",
        ratingLabel = "Rating",
        scoreLabel = "Score",
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
            AnimeApi.RatingGetAnime.G to "G – All ages",
            AnimeApi.RatingGetAnime.PG to "PG – Children",
            AnimeApi.RatingGetAnime.PG13 to "PG-13 – Teens",
            AnimeApi.RatingGetAnime.R17 to "R-17",
            AnimeApi.RatingGetAnime.R to "R+",
            AnimeApi.RatingGetAnime.RX to "Rx – Hentai"
        ),
        genreLabel = "Genre",
        genreCount = { n -> "$n genres" },
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
        episodesDescription = "Number of episodes",
        scoreDescription = "Average score out of 10",
        rankDescription = "Rank by score",
        popularityDescription = "Rank by popularity",
        membersDescription = "Number of members",
        studiosDescription = "Production studios",
        airedDescription = "Start airing date",
        statusDescription = "Airing status",
        synopsisLabel = "Synopsis",
        episodesLabel = "Episodes"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Alternative titles",
        airingDate = "Airing date",
        synopsis = "Synopsis",
        streams = "Available streams",
        loadingStreams = "Loading…",
        noStreams = "No stream found",
        couldNotLoadStreams = "Could not load streams",
        noSource = "No source selected",
        dub = "DUB",
        sub = "SUB",
        upNext = "Up next",
        nextEpisode = "Next episode",
        filler = "Filler",
        recap = "Recap",
        score = "Score",
        episodeNumber = "Episode {number}"
    ),
    player = PlayerStrings(
        play = "Play",
        pause = "Pause",
        fullscreen = "Enter fullscreen",
        exitFullscreen = "Exit fullscreen",
        speed = "Playback speed",
        audio = "Audio track",
        error = "Playback error"
    )
)



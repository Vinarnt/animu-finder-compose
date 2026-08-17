package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.jikan4k.models.AnimeTypes

private val languages = mapOf(
    Locales.EN to "English", Locales.FR to "French"
)

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnStrings = Strings(
    navigation = NavigationStrings(
        back = "Back"
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
        )
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
            AnimeTypes.TV to "TV",
            AnimeTypes.MOVIE to "Movie",
            AnimeTypes.OVA to "OVA",
            AnimeTypes.SPECIAL to "Special",
            AnimeTypes.ONA to "ONA",
            AnimeTypes.MUSIC to "Music",
            AnimeTypes.CM to "CM",
            AnimeTypes.PV to "PV",
            AnimeTypes.TV_SPECIAL to "TV Special"
        ),
        statusLabels = mapOf(
            AnimeSearchQueryStatus.AIRING to "Airing",
            AnimeSearchQueryStatus.COMPLETE to "Finished",
            AnimeSearchQueryStatus.UPCOMING to "Upcoming"
        ),
        ratingLabels = mapOf(
            AnimeSearchQueryRating.G to "G – All ages",
            AnimeSearchQueryRating.PG to "PG – Children",
            AnimeSearchQueryRating.PG13 to "PG-13 – Teens",
            AnimeSearchQueryRating.R17 to "R-17",
            AnimeSearchQueryRating.R to "R+",
            AnimeSearchQueryRating.RX to "Rx – Hentai"
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
        studiosDescription = "Production studios",
        airedDescription = "Start airing date",
        statusDescription = "Airing status",
        synopsisLabel = "Synopsis"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Alternative titles",
        airingDate = "Airing date",
        synopsis = "Synopsis",
        streams = "Available streams",
        noStreams = "No stream found",
        couldNotLoadStreams = "Could not load streams",
        noSource = "No source selected",
        dub = "DUB",
        sub = "SUB"
    )
)



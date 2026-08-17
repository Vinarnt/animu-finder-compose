package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.jikan4k.models.AnimeTypes

private val languages = mapOf(
    Locales.EN to "Anglais",
    Locales.FR to "Français"
)

@LyricistStrings(languageTag = Locales.FR)
val FrStrings = Strings(
    navigation = NavigationStrings(
        back = "Retour"
    ),
    ui = UIStrings(
        dropdown = DropdownStrings(
            search = "Rechercher",
            noContent = "Aucun élément correspondant"
        )
    ),
    settings = SettingStrings(
        title = "Préférences",
        theme = SettingThemeStrings(
            label = "Theme",
            values = SettingThemeValuesStrings(
                system = "Système",
                light = "Clair",
                dark = "Sombre"
            )
        ),
        locale = SettingLocaleStrings(
            label = "Langue"
        )
    ),
    languages = LanguagesStrings(
        locales = languages.keys.toList(),
        localeLabels = languages,
        getLocaleLabel = { locale -> languages[locale] ?: locale }
    ),
    animeList = AnimeListStrings(
        searchPlaceholder = "Rechercher un anime...",
        typeLabel = "Type",
        statusLabel = "Statut",
        ratingLabel = "Classification",
        scoreLabel = "Score",
        all = "Tous",
        typeLabels = mapOf(
            AnimeTypes.TV to "TV",
            AnimeTypes.MOVIE to "Film",
            AnimeTypes.OVA to "OVA",
            AnimeTypes.SPECIAL to "Spécial",
            AnimeTypes.ONA to "ONA",
            AnimeTypes.MUSIC to "Musique",
            AnimeTypes.CM to "CM",
            AnimeTypes.PV to "PV",
            AnimeTypes.TV_SPECIAL to "TV Spécial"
        ),
        statusLabels = mapOf(
            AnimeSearchQueryStatus.AIRING to "En cours",
            AnimeSearchQueryStatus.COMPLETE to "Terminé",
            AnimeSearchQueryStatus.UPCOMING to "À venir"
        ),
        ratingLabels = mapOf(
            AnimeSearchQueryRating.G to "G – Tout public",
            AnimeSearchQueryRating.PG to "PG – Enfants",
            AnimeSearchQueryRating.PG13 to "PG-13 – Ados",
            AnimeSearchQueryRating.R17 to "R-17",
            AnimeSearchQueryRating.R to "R+",
            AnimeSearchQueryRating.RX to "Rx – Hentai"
        ),
        genreLabel = "Genre",
        genreCount = { n -> "$n genres" },
        genreNames = mapOf(
            AnimeGenre.ACTION to "Action",
            AnimeGenre.ADVENTURE to "Aventure",
            AnimeGenre.COMEDY to "Comédie",
            AnimeGenre.AVANT_GARDE to "Avant-garde",
            AnimeGenre.MYSTERY to "Mystère",
            AnimeGenre.DRAMA to "Drame",
            AnimeGenre.ECCHI to "Ecchi",
            AnimeGenre.FANTASY to "Fantaisie",
            AnimeGenre.GAME to "Jeu",
            AnimeGenre.HISTORICAL to "Historique",
            AnimeGenre.HORROR to "Horreur",
            AnimeGenre.MARTIAL_ARTS to "Arts martiaux",
            AnimeGenre.MECHA to "Mecha",
            AnimeGenre.MUSIC to "Musique",
            AnimeGenre.PARODY to "Parodie",
            AnimeGenre.ROMANCE to "Romance",
            AnimeGenre.SCHOOL to "Scolaire",
            AnimeGenre.SCI_FI to "Science-fiction",
            AnimeGenre.SPACE to "Espace",
            AnimeGenre.SPORTS to "Sport",
            AnimeGenre.SUPER_POWER to "Super-pouvoirs",
            AnimeGenre.VAMPIRE to "Vampire",
            AnimeGenre.HAREM to "Harem",
            AnimeGenre.SLICE_OF_LIFE to "Tranche de vie",
            AnimeGenre.SUPERNATURAL to "Surnaturel",
            AnimeGenre.MILITARY to "Militaire",
            AnimeGenre.POLICE to "Police",
            AnimeGenre.PSYCHOLOGICAL to "Psychologique",
            AnimeGenre.SUSPENSE to "Suspense",
            AnimeGenre.AWARD_WINNING to "Primé",
            AnimeGenre.GOURMET to "Gastronomie"
        )
    ),
    animeDetail = AnimeDetailStrings(
        episodesDescription = "Nombre d'épisodes",
        scoreDescription = "Score moyen sur 10",
        rankDescription = "Classement par score",
        popularityDescription = "Classement par popularité",
        studiosDescription = "Studios de production",
        airedDescription = "Date de début de diffusion",
        statusDescription = "Statut de diffusion",
        synopsisLabel = "Synopsis"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Titres alternatifs",
        airingDate = "Date de diffusion",
        synopsis = "Synopsis",
        streams = "Streams disponibles",
        noStreams = "Aucun stream trouvé",
        couldNotLoadStreams = "Impossible de charger les streams",
        noSource = "Aucune source sélectionnée",
        dub = "VF",
        sub = "VOSTFR"
    )
)

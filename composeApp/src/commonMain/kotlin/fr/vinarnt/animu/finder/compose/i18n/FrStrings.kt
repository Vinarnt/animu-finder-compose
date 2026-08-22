package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.apis.AnimeApi

private val languages = mapOf(
    Locales.EN to "Anglais",
    Locales.FR to "Français"
)

@LyricistStrings(languageTag = Locales.FR)
val FrStrings = Strings(
    navigation = NavigationStrings(
        back = "Retour"
    ),
    home = HomeStrings(
        continueWatching = "Reprendre la lecture",
        resume = "Reprendre"
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
        ),
        appearance = "Apparence",
        playback = "Lecture",
        defaultQuality = "Qualité par défaut",
        preferredSubtitles = "Sous-titres préférés",
        autoplayNext = "Lecture automatique"
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
            AnimeApi.TypeGetAnime.TV to "TV",
            AnimeApi.TypeGetAnime.MOVIE to "Film",
            AnimeApi.TypeGetAnime.OVA to "OVA",
            AnimeApi.TypeGetAnime.SPECIAL to "Spécial",
            AnimeApi.TypeGetAnime.ONA to "ONA",
            AnimeApi.TypeGetAnime.MUSIC to "Musique",
            AnimeApi.TypeGetAnime.CM to "CM",
            AnimeApi.TypeGetAnime.PV to "PV",
            AnimeApi.TypeGetAnime.TV_SPECIAL to "TV Spécial"
        ),
        statusLabels = mapOf(
            AnimeApi.StatusGetAnime.AIRING to "En cours",
            AnimeApi.StatusGetAnime.COMPLETE to "Terminé",
            AnimeApi.StatusGetAnime.UPCOMING to "À venir"
        ),
        ratingLabels = mapOf(
            AnimeApi.RatingGetAnime.G to "G – Tout public",
            AnimeApi.RatingGetAnime.PG to "PG – Enfants",
            AnimeApi.RatingGetAnime.PG13 to "PG-13 – Ados",
            AnimeApi.RatingGetAnime.R17 to "R-17",
            AnimeApi.RatingGetAnime.R to "R+",
            AnimeApi.RatingGetAnime.RX to "Rx – Hentai"
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
        membersDescription = "Nombre de membres",
        studiosDescription = "Studios de production",
        airedDescription = "Date de début de diffusion",
        statusDescription = "Statut de diffusion",
        synopsisLabel = "Synopsis",
        episodesLabel = "Épisodes"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Titres alternatifs",
        airingDate = "Date de diffusion",
        synopsis = "Synopsis",
        streams = "Streams disponibles",
        loadingStreams = "Chargement…",
        noStreams = "Aucun stream trouvé",
        couldNotLoadStreams = "Impossible de charger les streams",
        noSource = "Aucune source sélectionnée",
        dub = "VF",
        sub = "VOSTFR",
        upNext = "À suivre",
        nextEpisode = "Épisode suivant",
        filler = "Filler",
        recap = "Récapitulatif",
        score = "Score",
        episodeNumber = "Épisode {number}"
    ),
    player = PlayerStrings(
        play = "Lecture",
        pause = "Pause",
        fullscreen = "Plein écran",
        exitFullscreen = "Quitter le plein écran",
        speed = "Vitesse de lecture",
        audio = "Piste audio",
        error = "Erreur de lecture"
    )
)

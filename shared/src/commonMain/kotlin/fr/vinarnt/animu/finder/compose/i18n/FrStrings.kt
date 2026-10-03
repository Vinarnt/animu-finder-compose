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
        back = "Retour",
        home = "Accueil",
        appName = "Animu Finder"
    ),
    home = HomeStrings(
        continueWatching = "Reprendre la lecture",
        episodeBadge = { n -> "EP $n" },
        featured = "À l'affiche",
        trending = "Tendances",
        newEpisodes = "Nouveaux épisodes",
        topRated = "Les mieux notés",
        action = "Action",
        fantasy = "Fantaisie",
        browsePanelSubtitle = "Rechercher et filtrer par type, statut, genre et score",
        browsePanelOpen = "Ouvrir ›"
    ),
    common = CommonStrings(
        unknown = "Inconnu",
        retry = "Réessayer",
        seeAll = "Tout voir ›",
        scrollLeft = "Défiler à gauche",
        scrollRight = "Défiler à droite",
        synopsis = "Synopsis",
        score = "Score"
    ),
    myList = MyListStrings(
        title = "Ma liste",
        emptyTitle = "Votre liste est vide",
        emptyHint = "Ouvrez un titre et appuyez sur « Ajouter à ma liste » pour l'enregistrer ici.",
        browseAnime = "Parcourir les animes",
        addLabel = "Ajouter à ma liste",
        addedLabel = "Ajouté à ma liste"
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
        general = "Général",
        playback = "Lecture",
        debug = "Débogage",
        defaultQuality = "Qualité par défaut",
        preferredSubtitles = "Sous-titres préférés",
        autoplayNext = "Lecture automatique",
        qualityAuto = "Auto",
        subtitleLanguageEnglish = "Anglais",
        subtitleLanguageFrench = "Français",
        subtitleLanguageJapanese = "Japonais",
        subtitleLanguageNone = "Aucun",
        cloudflare = SettingCloudflareStrings(
            label = "Cloudflare",
            description = "Certains fournisseurs sont protégés par Cloudflare. Résolvez la vérification une fois et Animu Finder réutilise le cookie, pour n'importe quel fournisseur.",
            openSite = "Ouvrir dans le navigateur",
            save = "Enregistrer",
            clear = "Effacer",
            statusConfigured = "Configuré",
            statusMissing = "Non configuré",
            captureHint = "Terminez la vérification Cloudflare, puis appuyez sur Capturer.",
            captureButton = "Capturer",
            captureFailed = "Cookie introuvable. Terminez d'abord la vérification.",
            captureSuccess = "Cookie capturé !",
            captureUnsupported = "La capture intégrée n'est pas disponible ici. Ouvrez le site dans votre navigateur, résolvez la vérification, puis collez le cookie cf_clearance ci-dessous.",
            solving = "En attente de Cloudflare…",
            cancel = "Annuler",
            cookieLabel = "Cookie cf_clearance",
            cookieHint = "Collez la valeur du cookie ou l'en-tête Cookie complet",
            userAgentLabel = "User-Agent",
            userAgentHint = "Le User-Agent utilisé lors de la création du cookie"
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
            AnimeApi.RatingGetAnime.G to "G : Tout public",
            AnimeApi.RatingGetAnime.PG to "PG : Enfants",
            AnimeApi.RatingGetAnime.PG13 to "PG-13 : Ados",
            AnimeApi.RatingGetAnime.R17 to "R-17",
            AnimeApi.RatingGetAnime.R to "R+",
            AnimeApi.RatingGetAnime.RX to "Rx : Hentai"
        ),
        genreLabel = "Genre",
        genreCount = { n -> "$n genres" },
        browseTitle = "Parcourir tous les animes",
        clearFilters = "Réinitialiser les filtres",
        emptyTitle = "Aucun anime ne correspond",
        emptyHint = "Essayez un autre titre ou assouplissez vos filtres.",
        resultCount = { n -> if (n == 1) "1 résultat" else "$n résultats" },
        sortLabel = "Trier",
        sortNewest = "Plus récents",
        sortOldest = "Plus anciens",
        sortAlphabetical = "A-Z",
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
        studiosDescription = "Studios de production",
        airedDescription = "Date de début de diffusion",
        statusDescription = "Statut de diffusion",
        episodesLabel = "Épisodes"
    ),
    episodeDetail = EpisodeDetailStrings(
        alternativeTitles = "Titres alternatifs",
        airingDate = "Date de diffusion",
        streams = "Streams disponibles",
        loadingStreams = "Chargement…",
        noStreams = "Aucun stream trouvé",
        noSource = "Aucune source sélectionnée",
        dub = "VF",
        sub = "VOSTFR",
        upNext = "À suivre",
        nextEpisode = "Épisode suivant",
        nowPlaying = "En lecture",
        filler = "Filler",
        recap = "Récapitulatif",
        episodeNumber = "Épisode {number}",
        episodeShort = "Ep {number}",
        episodeTitleFallback = { number -> "Épisode $number" }
    ),
    player = PlayerStrings(
        play = "Lecture",
        pause = "Pause",
        fullscreen = "Plein écran",
        exitFullscreen = "Quitter le plein écran",
        audio = "Piste audio",
        error = "Erreur de lecture"
    )
)

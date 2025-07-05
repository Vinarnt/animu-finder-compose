package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings

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
    )
)

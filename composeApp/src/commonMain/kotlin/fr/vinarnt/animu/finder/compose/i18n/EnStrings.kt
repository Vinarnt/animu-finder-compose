package fr.vinarnt.animu.finder.compose.i18n

import cafe.adriel.lyricist.LyricistStrings

private val languages = mapOf(
    Locales.EN to "English",
    Locales.FR to "French"
)

@LyricistStrings(languageTag = Locales.EN, default = true)
val EnStrings = Strings(
    navigation = NavigationStrings(
        back = "Back"
    ),
    ui = UIStrings(
        dropdown = DropdownStrings(
            search = "Search",
            noContent = "No item matching"
        )
    ),
    settings = SettingStrings(
        title = "Settings",
        theme = SettingThemeStrings(
            label = "Theme",
            values = SettingThemeValuesStrings(
                system = "System",
                light = "Light",
                dark = "Dark"
            )
        ),
        locale = SettingLocaleStrings(
            label = "Language"
        )
    ),
    languages = LanguagesStrings(
        locales = languages.keys.toList(),
        localeLabels = languages,
        getLocaleLabel = { locale -> languages[locale] ?: locale }
    )
)



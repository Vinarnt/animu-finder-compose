package fr.vinarnt.animu.finder.compose.i18n

data class Strings(
    val navigation: NavigationStrings,
    val ui: UIStrings,
    val settings: SettingStrings,
    val languages: LanguagesStrings
)

data class NavigationStrings(
    val back: String
)

data class SettingStrings(
    val title: String,
    val theme: SettingThemeStrings,
    val locale: SettingLocaleStrings
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

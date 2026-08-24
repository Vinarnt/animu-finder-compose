package fr.vinarnt.animu.finder.compose.i18n


import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import cafe.adriel.lyricist.LanguageTag
import cafe.adriel.lyricist.Lyricist
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings

public val StringsMap: Map<LanguageTag, Strings> = mapOf(
    "en" to EnStrings,
    "fr" to FrStrings
)

public val LocalStrings: ProvidableCompositionLocal<Strings> =
    staticCompositionLocalOf { EnStrings }

public val strings: Strings
    @Composable
    get() = LocalStrings.current

@Composable
public fun rememberStrings(
    defaultLanguageTag: LanguageTag = "en",
    currentLanguageTag: LanguageTag = Locale.current.toLanguageTag(),
): Lyricist<Strings> =
    rememberStrings(StringsMap, defaultLanguageTag, currentLanguageTag)

@Composable
public fun ProvideStrings(
    lyricist: Lyricist<Strings> = rememberStrings(),
    content: @Composable () -> Unit
) {
    ProvideStrings(lyricist, LocalStrings, content)
}

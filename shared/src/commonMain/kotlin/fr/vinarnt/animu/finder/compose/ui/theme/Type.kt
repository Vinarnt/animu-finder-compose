package fr.vinarnt.animu.finder.compose.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import animu_finder_compose.shared.generated.resources.Inter_Bold
import animu_finder_compose.shared.generated.resources.Inter_ExtraBold
import animu_finder_compose.shared.generated.resources.Inter_Medium
import animu_finder_compose.shared.generated.resources.Inter_Regular
import animu_finder_compose.shared.generated.resources.Inter_SemiBold
import animu_finder_compose.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

private val InterFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.Inter_Regular, FontWeight.Normal),
        Font(Res.font.Inter_Medium, FontWeight.Medium),
        Font(Res.font.Inter_SemiBold, FontWeight.SemiBold),
        Font(Res.font.Inter_Bold, FontWeight.Bold),
        Font(Res.font.Inter_ExtraBold, FontWeight.ExtraBold),
    )

private fun inter(
    family: FontFamily,
    weight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val AppBarTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.Bold, 15, 20)
val BrandTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.ExtraBold, 17, 22)
val SectionTitleTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.ExtraBold, 20, 26)
val ShelfTitleTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.Bold, 16, 22)
val KickerTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.Bold, 12, 16, 1f)
val HeroKickerTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.Bold, 11, 15, 1.5f)
val SynopsysTypography: TextStyle
    @Composable get() = inter(InterFamily, FontWeight.Normal, 14, 23)

@Composable
fun appTypography(): Typography {
    val family = InterFamily
    return Typography(
        displayLarge = inter(family, FontWeight.ExtraBold, 44, 48, -0.5f),
        displayMedium = inter(family, FontWeight.ExtraBold, 36, 40, -0.5f),
        displaySmall = inter(family, FontWeight.ExtraBold, 28, 31, -0.4f),
        headlineLarge = inter(family, FontWeight.ExtraBold, 30, 34, -0.4f),
        headlineMedium = inter(family, FontWeight.ExtraBold, 26, 30, -0.3f),
        headlineSmall = inter(family, FontWeight.Bold, 22, 27, -0.2f),
        titleLarge = inter(family, FontWeight.Bold, 17, 23),
        titleMedium = inter(family, FontWeight.SemiBold, 15, 21),
        titleSmall = inter(family, FontWeight.SemiBold, 14, 20),
        bodyLarge = inter(family, FontWeight.Normal, 16, 24),
        bodyMedium = inter(family, FontWeight.Normal, 14, 20),
        bodySmall = inter(family, FontWeight.Normal, 12, 18),
        labelLarge = inter(family, FontWeight.SemiBold, 13, 18),
        labelMedium = inter(family, FontWeight.Medium, 12, 16),
        labelSmall = inter(family, FontWeight.Medium, 11, 14),
    )
}
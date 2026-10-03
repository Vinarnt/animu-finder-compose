package fr.vinarnt.animu.finder.compose.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/**
 * The size class a width falls into, named on Material's window size classes: compact under 600dp,
 * medium under 840dp, expanded under 1200dp, then large. [Tv] is the app's own, above those.
 *
 * [Breakpoints] holds the widths. Read the class with [windowSizeClass], from a
 * `BoxWithConstraints` with the scope overload, or from a raw width with [from].
 */
enum class WindowSizeClass {
    /** A phone held upright. */
    Compact,

    /** A small tablet, or a phone on its side. */
    Medium,

    /** A tablet, or a small desktop window. */
    Expanded,

    /** A full desktop window. */
    Large,

    /** A television or a desktop window wider than any monitor usually gets. */
    Tv;

    /** Whether this class is [other] or wider than it. */
    infix fun isAtLeast(other: WindowSizeClass): Boolean = ordinal >= other.ordinal

    /** Whether this class is [other] or narrower than it. */
    infix fun isAtMost(other: WindowSizeClass): Boolean = ordinal <= other.ordinal

    companion object {
        /** The class a [width] falls into. */
        fun from(width: Dp): WindowSizeClass = when {
            width < Breakpoints.mediumMinWidth -> Compact
            width < Breakpoints.expandedMinWidth -> Medium
            width < Breakpoints.largeMinWidth -> Expanded
            width < Breakpoints.tvMinWidth -> Large
            else -> Tv
        }
    }
}

/** The size class of the nearest [BoxWithConstraints]' viewport. */
@Composable
@ReadOnlyComposable
fun BoxWithConstraintsScope.windowSizeClass(): WindowSizeClass =
    WindowSizeClass.from(maxWidth)

/** The size class of the window, for code that is not inside a [BoxWithConstraints]. */
@Composable
@ReadOnlyComposable
fun windowSizeClass(): WindowSizeClass =
    WindowSizeClass.from(LocalWindowWidth.current)

/**
 * Width of the window, published once at the app root.
 *
 * Defaults to [Dp.Infinity], so a reader outside the root gets the wide layout rather than silently
 * getting the phone one.
 *
 * Prefer a local `BoxWithConstraints` for branching: it measures only the branch that needs it. Use
 * this when the width decides something outside that tree, such as whether a screen owns the back
 * press.
 */
val LocalWindowWidth = staticCompositionLocalOf { Dp.Infinity }

/**
 * Publishes [LocalWindowWidth] to [content] from this scope's width. Wrap the app once, from a
 * `BoxWithConstraints` covering the window.
 */
@Composable
fun BoxWithConstraintsScope.ProvideWindowWidth(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWindowWidth provides maxWidth) {
        content()
    }
}

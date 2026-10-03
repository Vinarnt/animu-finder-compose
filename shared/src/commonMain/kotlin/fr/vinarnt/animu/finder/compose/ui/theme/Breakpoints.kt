package fr.vinarnt.animu.finder.compose.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout breakpoints, as window width. The first three are Material's window size classes;
 * [tvMinWidth] is the app's own.
 *
 * Prefer [WindowSizeClass] for branching, since it names the range and has the comparisons. Use
 * these when a rule needs a width of its own.
 */
object Breakpoints {
    /** Narrower than this is [WindowSizeClass.Compact]: a phone held upright. */
    val mediumMinWidth = 600.dp
    /** Narrower than this is [WindowSizeClass.Medium]: a small tablet, or a phone on its side. */
    val expandedMinWidth = 840.dp

    /** Narrower than this is [WindowSizeClass.Expanded]: a tablet, or a small desktop window. */
    val largeMinWidth = 1200.dp

    /** At or above this is [WindowSizeClass.Tv]: a television, or a very wide desktop window. */
    val tvMinWidth = 1600.dp

    /** Whether [width] is [sizeClass] or wider than it. */
    fun isAtLeast(width: Dp, sizeClass: WindowSizeClass): Boolean =
        WindowSizeClass.from(width) isAtLeast sizeClass

    /** Whether [width] is [sizeClass] or narrower than it. */
    fun isAtMost(width: Dp, sizeClass: WindowSizeClass): Boolean =
        WindowSizeClass.from(width) isAtMost sizeClass

    /** Whether [width] falls inside the half-open range `[from, until)`. */
    fun isWithin(width: Dp, from: Dp, until: Dp): Boolean = width >= from && width < until

    /** Whether [width] is [atLeast] but narrower than [below]. */
    fun isBetween(width: Dp, atLeast: Dp, below: Dp): Boolean = isWithin(width, atLeast, below)
}

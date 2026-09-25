package fr.vinarnt.animu.finder.compose.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Responsive layout breakpoints, expressed as window width.
 *
 * A layout is `compact` below [compactMaxWidth], `tablet` between [compactMaxWidth]
 * and [tabletMaxWidth], and `desktop` at or above [tabletMaxWidth]. Compare against
 * the `BoxWithConstraints` `maxWidth`, e.g. `val compact = maxWidth < Breakpoints.compactMaxWidth`.
 */
object Breakpoints {
    /** Exclusive upper bound of the compact (single-column) layout. */
    val compactMaxWidth = 760.dp

    /** Exclusive upper bound of the tablet layout; at or above this, desktop. */
    val tabletMaxWidth = 1024.dp
}

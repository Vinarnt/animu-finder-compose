package fr.vinarnt.animu.finder.compose.model

import kotlinx.serialization.Serializable

/**
 * Normalized position of the subtitle overlay relative to the player, in
 * fractions of the player's width/height (0..1). Storing fractions keeps the
 * subtitle at the same relative spot when the player is resized (e.g. entering
 * fullscreen).
 */
@Serializable
data class SubtitlePosition(
    val x: Float = 0.5f,
    // Just above the control bar so it does not sit on top of the player controls.
    val y: Float = 0.75f,
)
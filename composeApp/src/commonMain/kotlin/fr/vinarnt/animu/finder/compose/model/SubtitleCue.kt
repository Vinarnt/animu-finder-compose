package fr.vinarnt.animu.finder.compose.model

data class SubtitleCue(
    val startMs: Long,
    val endMs: Long,
    val text: String,
)
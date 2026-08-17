package fr.vinarnt.animu.finder.compose.model

enum class SubtitleType { Soft, Hard }

data class SubtitleTrack(
    val language: String?,
    val type: SubtitleType = SubtitleType.Soft,
    val url: String? = null,
    val text: String? = null,
)

data class StreamSource(
    val providerId: String,
    val url: String,
    val quality: String?,
    val isM3U8: Boolean,
    val headers: Map<String, String> = emptyMap(),
    val dub: String? = null,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val matchScore: Float = 1f,
)

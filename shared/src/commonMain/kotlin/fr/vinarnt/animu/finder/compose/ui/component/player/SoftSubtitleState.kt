package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import fr.vinarnt.animu.finder.compose.model.SubtitleCue
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import fr.vinarnt.animu.finder.compose.repository.SubtitleRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Shared soft-subtitle state for the players. External WebVTT subtitles
 * ([SubtitleTrack] of type [SubtitleType.Soft]) are fetched and parsed here.
 * The cue active at [currentTimeMs] is handed to the caller to render: a
 * Compose overlay on desktop/Android/iOS (via [SubtitleOverlay]) or a DOM
 * node on the web player. Hard subs are burned into the video and need no
 * handling.
 */
@Composable
fun rememberSoftSubtitleState(
    subtitles: List<SubtitleTrack>,
    currentTimeMs: Long,
): SoftSubtitleState {
    val repository = koinInject<SubtitleRepository>()
    val settings = koinInject<SettingManager>()
    val scope = rememberCoroutineScope()

    val softUrl = subtitles
        .firstOrNull { it.type == SubtitleType.Soft && !it.url.isNullOrBlank() }
        ?.url

    var cues by remember { mutableStateOf<List<SubtitleCue>>(emptyList()) }
    var position by remember { mutableStateOf(SubtitlePosition()) }

    LaunchedEffect(softUrl) {
        cues = if (softUrl != null) repository.load(softUrl) else emptyList()
    }
    LaunchedEffect(Unit) {
        position = settings.getSubtitlePosition()
    }

    val activeCue = remember(cues, currentTimeMs) {
        cues.firstOrNull { currentTimeMs in it.startMs until it.endMs }
    }

    return SoftSubtitleState(
        activeCue = activeCue,
        position = position,
        movePosition = { position = it },
        commitPosition = { scope.launch { settings.setSubtitlePosition(position) } },
    )
}

class SoftSubtitleState(
    val activeCue: SubtitleCue?,
    val position: SubtitlePosition,
    /** Updates the in-memory position (e.g. while the user drags the overlay). */
    val movePosition: (SubtitlePosition) -> Unit,
    /** Persists the current position to settings. */
    val commitPosition: () -> Unit,
)
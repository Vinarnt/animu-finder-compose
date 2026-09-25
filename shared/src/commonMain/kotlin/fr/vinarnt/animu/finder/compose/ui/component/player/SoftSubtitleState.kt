package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.vinarnt.animu.finder.compose.model.SubtitleCue
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.viewmodel.SubtitleViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shared soft-subtitle state for the players. External WebVTT subtitles
 * ([SubtitleTrack]) are fetched and parsed by [SubtitleViewModel]. The cue
 * active at [currentTimeMs] is handed to the caller to render: a Compose
 * overlay on desktop/Android/iOS (via [SubtitleOverlay]) or a DOM node on the
 * web player. Hard subs are burned into the video and need no handling.
 */
@Composable
fun rememberSoftSubtitleState(
    subtitles: List<SubtitleTrack>,
    currentTimeMs: Long,
): SoftSubtitleState {
    val vm: SubtitleViewModel = koinViewModel()
    val cues by vm.cues.collectAsStateWithLifecycle()
    val position by vm.position.collectAsStateWithLifecycle()

    LaunchedEffect(subtitles) {
        vm.load(subtitles)
    }

    val activeCue = remember(cues, currentTimeMs) {
        cues.firstOrNull { currentTimeMs in it.startMs until it.endMs }
    }

    return SoftSubtitleState(
        activeCue = activeCue,
        position = position,
        movePosition = vm::movePosition,
        commitPosition = vm::commitPosition,
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

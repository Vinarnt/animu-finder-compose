package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vinarnt.animu.finder.compose.model.SubtitleCue
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import fr.vinarnt.animu.finder.compose.repository.SubtitleRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns soft-subtitle loading and the persisted overlay position, so the player
 * composables do not touch [SubtitleRepository]/[SettingManager] directly.
 */
class SubtitleViewModel(
    private val subtitleRepository: SubtitleRepository,
    private val settingManager: SettingManager,
) : ViewModel() {

    private val _cues = MutableStateFlow<List<SubtitleCue>>(emptyList())
    val cues: StateFlow<List<SubtitleCue>> = _cues.asStateFlow()

    private val _position = MutableStateFlow(SubtitlePosition())
    val position: StateFlow<SubtitlePosition> = _position.asStateFlow()

    init {
        viewModelScope.launch { _position.value = settingManager.getSubtitlePosition() }
    }

    /** Loads the external WebVTT cues for [subtitles], if any soft track is present. */
    fun load(subtitles: List<SubtitleTrack>) {
        val softUrl = subtitles
            .firstOrNull { it.type == SubtitleType.Soft && !it.url.isNullOrBlank() }
            ?.url
        viewModelScope.launch {
            _cues.value = if (softUrl != null) subtitleRepository.load(softUrl) else emptyList()
        }
    }

    /** Updates the in-memory position (e.g. while the user drags the overlay). */
    fun movePosition(position: SubtitlePosition) {
        _position.value = position
    }

    /** Persists the current position to settings. */
    fun commitPosition() {
        viewModelScope.launch { settingManager.setSubtitlePosition(_position.value) }
    }
}

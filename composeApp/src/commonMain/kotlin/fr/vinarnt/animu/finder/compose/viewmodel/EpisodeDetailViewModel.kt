package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.repository.extractor.EpisodeSearchQuery
import fr.vinarnt.animu.finder.compose.repository.extractor.ProviderError
import fr.vinarnt.animu.finder.compose.repository.extractor.StreamRepository
import fr.vinarnt.jikan4k.models.AnimeEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EpisodeDetailViewModel(
    private val animeRepository: AnimeRepository,
    private val streamRepository: StreamRepository,
) : ViewModel() {

    private val _episode = MutableStateFlow<AnimeEpisode?>(null)
    val episode: StateFlow<AnimeEpisode?> = _episode.asStateFlow()

    private val _streams = MutableStateFlow<List<StreamSource>>(emptyList())
    val streams: StateFlow<List<StreamSource>> = _streams.asStateFlow()

    private val _streamErrors = MutableStateFlow<List<ProviderError>>(emptyList())
    val streamErrors: StateFlow<List<ProviderError>> = _streamErrors.asStateFlow()

    private val _selectedStream = MutableStateFlow<StreamSource?>(null)
    val selectedStream: StateFlow<StreamSource?> = _selectedStream.asStateFlow()

    private val _loadingStreams = MutableStateFlow(false)
    val loadingStreams: StateFlow<Boolean> = _loadingStreams.asStateFlow()

    private var episodeLoadGeneration = 0
    private var streamLoadGeneration = 0

    fun loadEpisode(animeId: Int, episodeNumber: Int) {
        val generation = ++episodeLoadGeneration
        _episode.value = null
        viewModelScope.launch {
            try {
                val result = animeRepository.getAnimeEpisodeById(animeId, episodeNumber)
                if (generation != episodeLoadGeneration) return@launch
                _episode.value = result
            } catch (e: Exception) {
                Logger.w("Failed to load episode: ${e.message}", e)
            }
        }
    }

    fun loadStreams(query: EpisodeSearchQuery) {
        val generation = ++streamLoadGeneration
        _loadingStreams.value = true
        _streams.value = emptyList()
        _selectedStream.value = null
        _streamErrors.value = emptyList()
        viewModelScope.launch {
            try {
                val result = streamRepository.getStreams(query)
                if (generation != streamLoadGeneration) return@launch
                _streams.value = result.streams.distinctBy { it.url }
                _streamErrors.value = result.errors
                _selectedStream.value = result.streams.firstOrNull()
            } catch (e: Exception) {
                if (generation != streamLoadGeneration) return@launch
                Logger.w("Failed to load streams: ${e.message}", e)
                _streams.value = emptyList()
                _streamErrors.value = listOf(
                    ProviderError("all", "All providers", e.message ?: "unknown error")
                )
            } finally {
                if (generation == streamLoadGeneration) {
                    _loadingStreams.value = false
                }
            }
        }
    }

    fun selectStream(source: StreamSource) {
        _selectedStream.value = source
    }
}

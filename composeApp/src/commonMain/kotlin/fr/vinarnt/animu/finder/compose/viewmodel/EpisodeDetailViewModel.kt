package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.repository.provider.EpisodeSearchQuery
import fr.vinarnt.animu.finder.compose.repository.provider.StreamRepository
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class EpisodeDetailViewModel(
    private val animeRepository: AnimeRepository,
    private val streamRepository: StreamRepository,
) : ViewModel() {

    private val _episode = MutableStateFlow<GetAnimeByIdEpisodesByEpisodeId200ResponseData?>(null)
    val episode: StateFlow<GetAnimeByIdEpisodesByEpisodeId200ResponseData?> = _episode.asStateFlow()

    private val _streams = MutableStateFlow<List<StreamSource>>(emptyList())
    val streams: StateFlow<List<StreamSource>> = _streams.asStateFlow()

    private val _selectedStream = MutableStateFlow<StreamSource?>(null)
    val selectedStream: StateFlow<StreamSource?> = _selectedStream.asStateFlow()

    private val _loadingStreams = MutableStateFlow(false)
    val loadingStreams: StateFlow<Boolean> = _loadingStreams.asStateFlow()

    private val _nextEpisode = MutableStateFlow<GetAnimeByIdEpisodesByEpisodeId200ResponseData?>(null)
    val nextEpisode: StateFlow<GetAnimeByIdEpisodesByEpisodeId200ResponseData?> = _nextEpisode.asStateFlow()

    private var episodeLoadGeneration = 0
    private var nextEpisodeLoadGeneration = 0
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

    fun loadNextEpisode(animeId: Int, episodeNumber: Int) {
        val generation = ++nextEpisodeLoadGeneration
        _nextEpisode.value = null
        viewModelScope.launch {
            try {
                val result = animeRepository.getAnimeEpisodeById(animeId, episodeNumber)
                if (generation != nextEpisodeLoadGeneration) return@launch
                _nextEpisode.value = result
            } catch (e: Exception) {
                Logger.w("Failed to load next episode: ${e.message}", e)
            }
        }
    }

    fun loadStreams(query: EpisodeSearchQuery) {
        val generation = ++streamLoadGeneration
        _loadingStreams.value = true
        _streams.value = emptyList()
        _selectedStream.value = null
        viewModelScope.launch {
            try {
                streamRepository.getStreams(query)
                    .onCompletion {
                        if (generation == streamLoadGeneration) _loadingStreams.value = false
                    }
                    .collect { result ->
                        if (generation != streamLoadGeneration) return@collect
                        _streams.value = (_streams.value + result.streams).distinctBy { it.url }
                        if (_selectedStream.value == null) {
                            _selectedStream.value = result.streams.firstOrNull()
                        }
                    }
            } catch (e: Exception) {
                Logger.w("Failed to load streams: ${e.message}", e)
                if (generation == streamLoadGeneration) _loadingStreams.value = false
            }
        }
    }

    fun selectStream(source: StreamSource) {
        _selectedStream.value = source
    }
}

package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.ContinueWatchingEntry
import fr.vinarnt.animu.finder.compose.model.StreamPlayability
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.currentStreamPlatform
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.repository.provider.EpisodeSearchQuery
import fr.vinarnt.animu.finder.compose.repository.provider.StreamRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.animu.finder.compose.util.animeArtworkUrl
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class EpisodeDetailViewModel(
    private val animeRepository: AnimeRepository,
    private val streamRepository: StreamRepository,
    private val settingManager: SettingManager,
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

    private val _episodeList = MutableStateFlow<List<GetAnimeByIdEpisodes200ResponseDataInner>>(emptyList())
    val episodeList: StateFlow<List<GetAnimeByIdEpisodes200ResponseDataInner>> = _episodeList.asStateFlow()

    private val _artworkUrl = MutableStateFlow<String?>(null)
    val artworkUrl: StateFlow<String?> = _artworkUrl.asStateFlow()

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
                        val platform = currentStreamPlatform
                        val playable = result.streams.filter { platform in StreamPlayability.platformsFor(it.url) }
                        _streams.value = (_streams.value + playable).distinctBy { it.url }
                        if (_selectedStream.value == null) {
                            _selectedStream.value = playable.firstOrNull()
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

    /** Records this episode as the latest watched progress for the title. */
    fun addToContinueWatching(animeId: Int, episodeNumber: Int) {
        viewModelScope.launch {
            settingManager.addContinueWatching(
                ContinueWatchingEntry(animeId = animeId, episodeNumber = episodeNumber)
            )
        }
    }

    /** Loads the full episode list so the episode shelf can switch between episodes. */
    fun loadEpisodeList(animeId: Int) {
        viewModelScope.launch {
            try {
                val page = animeRepository.getAnimeEpisodes(animeId, 1)
                _episodeList.value = page.data
            } catch (e: Exception) {
                Logger.w("Failed to load episode list: ${e.message}", e)
            }
        }
    }

    /** The title's artwork, used as a fallback for episodes that have no image of their own. */
    fun loadArtwork(animeId: Int) {
        viewModelScope.launch {
            try {
                _artworkUrl.value = animeArtworkUrl(animeRepository.getAnimeById(animeId))
            } catch (e: Exception) {
                Logger.w("Failed to load artwork: ${e.message}", e)
            }
        }
    }
}

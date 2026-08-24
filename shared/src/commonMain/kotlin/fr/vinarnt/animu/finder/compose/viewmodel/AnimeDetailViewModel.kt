package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200ResponseDataInner
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AnimeDetailViewModel(
    private val animeRepository: AnimeRepository,
    private val settingManager: SettingManager,
) : ViewModel() {
    private val _anime: MutableStateFlow<GetAnimeById200ResponseData?> = MutableStateFlow(null)
    val anime: StateFlow<GetAnimeById200ResponseData?> = _anime.asStateFlow()

    val episodeDisplay: StateFlow<EpisodeDisplay> = settingManager.getEpisodeDisplay()
        .stateIn(viewModelScope, SharingStarted.Eagerly, EpisodeDisplay.POSTER)

    private val _episodesPaginationState = MutableStateFlow(createEpisodesPaginationState(-1))
    val episodesPaginationState: StateFlow<PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner>> =
        _episodesPaginationState.asStateFlow()


    fun setEpisodeDisplay(display: EpisodeDisplay) {
        viewModelScope.launch { settingManager.setEpisodeDisplay(display) }
    }


    suspend fun getAnime(malId: Int) {
        _anime.value = null
        _episodesPaginationState.value = createEpisodesPaginationState(malId)

        animeRepository.getAnimeById(malId).let {
            _anime.value = it
        }
    }

    private fun createEpisodesPaginationState(malId: Int): PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner> {
        lateinit var state: PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner>
        state = PaginationState(
            initialPageKey = 1,
            onRequestPage = { page -> fetchEpisodes(malId, page, state) }
        )

        return state
    }

    private fun fetchEpisodes(malId: Int, page: Int, state: PaginationState<Int, GetAnimeByIdEpisodes200ResponseDataInner>) {
        viewModelScope.launch {
            try {
                animeRepository.getAnimeEpisodes(malId, page).let {
                    state.appendPage(
                        items = it.data,
                        isLastPage = it.pagination.hasNextPage != true,
                        nextPageKey = page + 1,
                    )
                }
            } catch (e: Exception) {
                state.setError(e)
            }
        }
    }

    override fun onCleared() {
        Logger.d("Cleared AnimeDetailViewModel")
    }
}
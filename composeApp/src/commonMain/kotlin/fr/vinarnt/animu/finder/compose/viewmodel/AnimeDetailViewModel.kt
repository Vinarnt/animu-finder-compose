package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.jikan4k.models.Anime
import fr.vinarnt.jikan4k.models.AnimeEpisodesAllOfData
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AnimeDetailViewModel(private val animeRepository: AnimeRepository) : ViewModel() {
    private val _anime: MutableStateFlow<Anime?> = MutableStateFlow(null)
    val anime: StateFlow<Anime?> = _anime.asStateFlow()

    private val _episodesPaginationState = MutableStateFlow(createEpisodesPaginationState(-1))
    val episodesPaginationState: StateFlow<PaginationState<Int, AnimeEpisodesAllOfData>> =
        _episodesPaginationState.asStateFlow()


    suspend fun getAnime(malId: Int) {
        _anime.value = null
        _episodesPaginationState.value = createEpisodesPaginationState(malId)

        animeRepository.getAnimeById(malId).let {
            _anime.value = it
        }
    }

    private fun createEpisodesPaginationState(malId: Int): PaginationState<Int, AnimeEpisodesAllOfData> {
        lateinit var state: PaginationState<Int, AnimeEpisodesAllOfData>
        state = PaginationState(
            initialPageKey = 1,
            onRequestPage = { page -> fetchEpisodes(malId, page, state) }
        )

        return state
    }

    private fun fetchEpisodes(malId: Int, page: Int, state: PaginationState<Int, AnimeEpisodesAllOfData>) {
        viewModelScope.launch {
            try {
                animeRepository.getAnimeEpisodes(malId, page).let {
                    state.appendPage(
                        items = it.data ?: emptyList(),
                        isLastPage = it.pagination?.hasNextPage != true,
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
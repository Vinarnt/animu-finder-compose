package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.jikan4k.models.Anime
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.jikan4k.models.AnimeTypes
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class AnimeSearchFilters(
    val queryText: String = "",
    val scoreRange: ClosedFloatingPointRange<Float> = 1f..10f,
    val type: AnimeTypes? = null,
    val status: AnimeSearchQueryStatus? = null,
    val rating: AnimeSearchQueryRating? = null,
    val genres: Set<AnimeGenre> = emptySet()
)

class AnimeListViewModel(private val animeRepository: AnimeRepository) : ViewModel() {

    private val _filters = MutableStateFlow(AnimeSearchFilters())
    val filters: StateFlow<AnimeSearchFilters> = _filters.asStateFlow()

    private val _paginationState = MutableStateFlow(createPaginationState(AnimeSearchFilters()))
    val paginationState: StateFlow<PaginationState<Int, Anime>> = _paginationState.asStateFlow()

    private var searchDebounceJob: Job? = null

    fun updateQuery(text: String) {
        searchDebounceJob?.cancel()
        _filters.value = _filters.value.copy(queryText = text)
        searchDebounceJob = viewModelScope.launch {
            delay(400.milliseconds)
            _paginationState.value = createPaginationState(_filters.value)
        }
    }

    fun updateType(type: AnimeTypes?) {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value.copy(type = type))
    }

    fun updateStatus(status: AnimeSearchQueryStatus?) {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value.copy(status = status))
    }

    fun updateRating(rating: AnimeSearchQueryRating?) {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value.copy(rating = rating))
    }

    fun updateGenres(genres: Set<AnimeGenre>) {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value.copy(genres = genres))
    }

    fun updateScoreRange(range: ClosedFloatingPointRange<Float>) {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value.copy(scoreRange = range))
    }

    fun commitScoreRange() {
        searchDebounceJob?.cancel()
        applyFilters(_filters.value)
    }

    private fun applyFilters(newFilters: AnimeSearchFilters) {
        _filters.value = newFilters
        _paginationState.value = createPaginationState(newFilters)
    }

    private fun createPaginationState(appliedFilters: AnimeSearchFilters): PaginationState<Int, Anime> {
        lateinit var state: PaginationState<Int, Anime>
        state = PaginationState(
            initialPageKey = 1,
            onRequestPage = { page -> fetchAnimes(appliedFilters, page, state) }
        )
        return state
    }

    private fun fetchAnimes(fetchFilters: AnimeSearchFilters, page: Int, state: PaginationState<Int, Anime>) {
        viewModelScope.launch {
            try {
                animeRepository.searchAnimes(
                    page = page,
                    query = fetchFilters.queryText.takeIf { it.isNotBlank() },
                    type = fetchFilters.type,
                    status = fetchFilters.status,
                    rating = fetchFilters.rating,
                    minScore = fetchFilters.scoreRange.start.takeIf { it > 1f }?.toDouble(),
                    maxScore = fetchFilters.scoreRange.endInclusive.takeIf { it < 10f }?.toDouble(),
                    genres = fetchFilters.genres
                ).let {
                    state.appendPage(
                        items = it.data ?: emptyList(),
                        isLastPage = it.pagination?.hasNextPage != true,
                        nextPageKey = page + 1
                    )
                }
            } catch (e: Exception) {
                state.setError(e)
            }
        }
    }

    override fun onCleared() {
        Logger.d("Cleared AnimeListViewModel")
    }
}
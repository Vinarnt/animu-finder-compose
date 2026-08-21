package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.model.ContinueWatchingEntry
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.jikan4k.apis.AnimeApi
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_RESUME = 8

data class ContinueWatchingItem(
    val entry: ContinueWatchingEntry,
    val anime: GetAnimeById200ResponseData?,
)

data class AnimeSearchFilters(
    val queryText: String = "",
    val scoreRange: ClosedFloatingPointRange<Float> = 1f..10f,
    val type: AnimeApi.TypeGetAnime? = null,
    val status: AnimeApi.StatusGetAnime? = null,
    val rating: AnimeApi.RatingGetAnime? = null,
    val genres: Set<AnimeGenre> = emptySet()
)

class AnimeListViewModel(
    private val animeRepository: AnimeRepository,
    private val settingManager: SettingManager,
) : ViewModel() {

    private val _filters = MutableStateFlow(AnimeSearchFilters())
    val filters: StateFlow<AnimeSearchFilters> = _filters.asStateFlow()

    private val _paginationState = MutableStateFlow(createPaginationState(AnimeSearchFilters()))
    val paginationState: StateFlow<PaginationState<Int, GetAnime200ResponseDataInner>> = _paginationState.asStateFlow()

    val continueWatching: StateFlow<List<ContinueWatchingEntry>> =
        settingManager.getWatchHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _resumeItems = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val resumeItems: StateFlow<List<ContinueWatchingItem>> = _resumeItems.asStateFlow()

    init {
        viewModelScope.launch {
            continueWatching.collect { entries ->
                val items = entries.take(MAX_RESUME).map { ContinueWatchingItem(it, null) }
                _resumeItems.value = items
                items.forEach { item ->
                    viewModelScope.launch {
                        val anime = runCatching { animeRepository.getAnimeById(item.entry.animeId) }.getOrNull()
                        _resumeItems.update { list ->
                            list.map {
                                if (it.entry.animeId == item.entry.animeId) it.copy(anime = anime) else it
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateQuery(text: String) {
        _filters.value = _filters.value.copy(queryText = text)
    }

    fun updateType(type: AnimeApi.TypeGetAnime?) {
        applyFilters(_filters.value.copy(type = type))
    }

    fun updateStatus(status: AnimeApi.StatusGetAnime?) {
        applyFilters(_filters.value.copy(status = status))
    }

    fun updateRating(rating: AnimeApi.RatingGetAnime?) {
        applyFilters(_filters.value.copy(rating = rating))
    }

    fun updateGenres(genres: Set<AnimeGenre>) {
        applyFilters(_filters.value.copy(genres = genres))
    }

    fun updateScoreRange(range: ClosedFloatingPointRange<Float>) {
        _filters.value = _filters.value.copy(scoreRange = range)
    }

    fun applyCurrentFilters() {
        applyFilters(_filters.value)
    }

    private fun applyFilters(newFilters: AnimeSearchFilters) {
        _filters.value = newFilters
        _paginationState.value = createPaginationState(newFilters)
    }

    private fun createPaginationState(appliedFilters: AnimeSearchFilters): PaginationState<Int, GetAnime200ResponseDataInner> {
        lateinit var state: PaginationState<Int, GetAnime200ResponseDataInner>
        state = PaginationState(
            initialPageKey = 1,
            onRequestPage = { page -> fetchAnimes(appliedFilters, page, state) }
        )
        return state
    }

    private fun fetchAnimes(fetchFilters: AnimeSearchFilters, page: Int, state: PaginationState<Int, GetAnime200ResponseDataInner>) {
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
                        items = it.data,
                        isLastPage = it.pagination.hasNextPage != true,
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
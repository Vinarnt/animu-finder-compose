package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.jikan4k.apis.AnimeApi
import fr.vinarnt.jikan4k.models.GetAnime200ResponseDataInner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads the distinct home shelves: each shelf runs its own query so the rows
 * don't all show the same top-scored anime (Trending = popularity, New episodes =
 * newest airing, Top rated = score, Action/Fantasy = genre).
 */
class HomeShelvesViewModel(
    private val animeRepository: AnimeRepository,
) : ViewModel() {

    private val _trending = MutableStateFlow<List<GetAnime200ResponseDataInner>>(emptyList())
    val trending: StateFlow<List<GetAnime200ResponseDataInner>> = _trending.asStateFlow()

    private val _newEpisodes = MutableStateFlow<List<GetAnime200ResponseDataInner>>(emptyList())
    val newEpisodes: StateFlow<List<GetAnime200ResponseDataInner>> = _newEpisodes.asStateFlow()

    private val _topRated = MutableStateFlow<List<GetAnime200ResponseDataInner>>(emptyList())
    val topRated: StateFlow<List<GetAnime200ResponseDataInner>> = _topRated.asStateFlow()

    private val _action = MutableStateFlow<List<GetAnime200ResponseDataInner>>(emptyList())
    val action: StateFlow<List<GetAnime200ResponseDataInner>> = _action.asStateFlow()

    private val _fantasy = MutableStateFlow<List<GetAnime200ResponseDataInner>>(emptyList())
    val fantasy: StateFlow<List<GetAnime200ResponseDataInner>> = _fantasy.asStateFlow()

    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        loadTrending()
        loadNewEpisodes()
        loadTopRated()
        loadGenre(ActionGenre, _action)
        loadGenre(FantasyGenre, _fantasy)
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _trending.value = runCatching {
                animeRepository.searchAnimes(limit = 12, orderBy = AnimeApi.OrderByGetAnime.MEMBERS).data
            }.getOrElse { emptyList() }
        }
    }

    private fun loadNewEpisodes() {
        viewModelScope.launch {
            _newEpisodes.value = runCatching {
                animeRepository.searchAnimes(limit = 12, orderBy = AnimeApi.OrderByGetAnime.START_DATE).data
            }.getOrElse { emptyList() }
        }
    }

    private fun loadTopRated() {
        viewModelScope.launch {
            _topRated.value = runCatching {
                animeRepository.searchAnimes(limit = 12, orderBy = AnimeApi.OrderByGetAnime.SCORE).data
            }.getOrElse { emptyList() }
        }
    }

    private fun loadGenre(genre: AnimeGenre, target: MutableStateFlow<List<GetAnime200ResponseDataInner>>) {
        viewModelScope.launch {
            target.value = runCatching {
                animeRepository.searchAnimes(limit = 12, genres = setOf(genre)).data
            }.getOrElse { emptyList() }
        }
    }

    private companion object {
        val ActionGenre = AnimeGenre.ACTION
        val FantasyGenre = AnimeGenre.FANTASY
    }
}
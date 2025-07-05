package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.jikan4k.models.Anime
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.launch


class AnimeListViewModel(private val animeRepository: AnimeRepository) : ViewModel() {

    val paginationState = PaginationState<Int, Anime>(
        initialPageKey = 1,
        onRequestPage = { getTopAnimes(it) }
    )

    private fun getTopAnimes(page: Int = 1) {
        viewModelScope.launch {
            try {
                animeRepository.getTopAnimes(page).let {
                    paginationState.appendPage(
                        items = it.data!!,
                        isLastPage = !it.pagination!!.hasNextPage!!,
                        nextPageKey = it.pagination!!.currentPage!! + 1,
                    )
                }
            } catch (e: Exception) {
                paginationState.setError(e)
            }
        }
    }

    override fun onCleared() {
        Logger.d("Cleared AnimeListViewModel")
    }
}

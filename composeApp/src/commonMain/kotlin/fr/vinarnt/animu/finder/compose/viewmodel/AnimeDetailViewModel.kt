package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.jikan4k.models.Anime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AnimeDetailViewModel(private val animeRepository: AnimeRepository) : ViewModel() {
    private val _anime: MutableStateFlow<Anime?> = MutableStateFlow(null)
    val anime: StateFlow<Anime?> = _anime.asStateFlow()

    suspend fun getAnime(malId: Int) {
        animeRepository.getAnimeById(malId).let {
            _anime.value = it
        }
    }

    override fun onCleared() {
        Logger.d("Cleared AnimeDetailViewModel")
    }
}

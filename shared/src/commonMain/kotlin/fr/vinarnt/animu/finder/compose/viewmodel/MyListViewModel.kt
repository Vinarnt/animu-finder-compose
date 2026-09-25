package fr.vinarnt.animu.finder.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.service.SettingManager
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyListViewModel(
    private val animeRepository: AnimeRepository,
    private val settingManager: SettingManager,
) : ViewModel() {

    val myList: StateFlow<List<Int>> = settingManager.getMyList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _items = MutableStateFlow<List<GetAnimeById200ResponseData>>(emptyList())
    val items: StateFlow<List<GetAnimeById200ResponseData>> = _items.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var loadedIds: Set<Int> = emptySet()

    init {
        viewModelScope.launch {
            myList.collect { load(it) }
        }
    }

    private fun load(ids: List<Int>) {
        val distinct = ids.distinct()
        if (distinct == loadedIds.toList()) return
        loadedIds = distinct.toSet()
        _loading.value = distinct.isNotEmpty()
        viewModelScope.launch {
            val result = distinct.mapNotNull { id ->
                runCatching { animeRepository.getAnimeById(id) }.getOrNull()
            }
            _items.value = result
            _loading.value = false
        }
    }
}

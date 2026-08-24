package fr.vinarnt.animu.finder.compose.repository

sealed interface ResponseState<out T> {
    data class Success<T>(val data: T) : ResponseState<T>
    data class Error(val exception: Throwable?) : ResponseState<Nothing>
    data object Loading : ResponseState<Nothing>
    data object None : ResponseState<Nothing>
}

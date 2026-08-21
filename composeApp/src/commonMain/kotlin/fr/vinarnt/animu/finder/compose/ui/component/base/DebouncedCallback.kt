package fr.vinarnt.animu.finder.compose.ui.component.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A reusable callback that defers its invocation until [debounceMs] of inactivity.
 *
 * The debounce is automatically cancelled when the composable that created it leaves
 * composition ([rememberCoroutineScope]).
 *
 * - [invoke] cancels any pending invocation and schedules [onDebounced] after [debounceMs].
 * - [flush] applies [onDebounced] immediately, dropping any pending invocation.
 * - [cancel] drops any pending invocation without applying it.
 */
class DebouncedCallback(
    private val debounceMs: Long,
    private val scope: CoroutineScope,
    private val onDebounced: State<() -> Unit>,
) {
    private var job: Job? = null

    operator fun invoke() {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            onDebounced.value()
        }
    }

    fun flush() {
        job?.cancel()
        job = null
        onDebounced.value()
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}

@Composable
fun rememberDebouncedCallback(
    debounceMs: Long = 400L,
    onDebounced: () -> Unit,
): DebouncedCallback {
    val scope = rememberCoroutineScope()
    val onDebouncedState = rememberUpdatedState(onDebounced)
    return remember(debounceMs, scope) {
        DebouncedCallback(debounceMs, scope, onDebouncedState)
    }
}
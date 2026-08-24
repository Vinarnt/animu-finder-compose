package fr.vinarnt.animu.finder.compose.repository.provider

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StreamRepository(
    private val providers: List<StreamingProvider>,
) {

    fun getStreams(query: EpisodeSearchQuery): Flow<StreamsResult> = callbackFlow {
        val mutex = Mutex()
        var remaining = providers.size

        providers.forEach { provider ->
            launch {
                val result = try {
                    StreamsResult(
                        streams = provider.extractStreams(query),
                        errors = emptyList(),
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Logger.w("Provider ${provider.providerId} failed: ${e.message}", e)
                    StreamsResult(
                        streams = emptyList(),
                        errors = listOf(
                            ProviderError(
                                providerId = provider.providerId,
                                providerName = provider.providerName,
                                message = e.message ?: e::class.simpleName ?: "unknown error",
                            )
                        ),
                    )
                }
                trySend(result)
                val done = mutex.withLock {
                    remaining--
                    remaining == 0
                }
                if (done) close()
            }
        }
        awaitClose()
    }
}

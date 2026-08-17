package fr.vinarnt.animu.finder.compose.repository.extractor

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StreamRepository(
    private val extractors: List<StreamingExtractor>,
) {

    suspend fun getStreams(query: EpisodeSearchQuery): StreamsResult = coroutineScope {
        data class Outcome(
            val streams: List<StreamSource> = emptyList(),
            val error: ProviderError? = null,
        )

        val outcomes = extractors.map { extractor ->
            async {
                try {
                    Outcome(streams = extractor.extractStreams(query))
                } catch (e: Exception) {
                    Logger.w("Extractor ${extractor.providerId} failed: ${e.message}", e)
                    Outcome(
                        error = ProviderError(
                            providerId = extractor.providerId,
                            providerName = extractor.providerName,
                            message = e.message ?: e::class.simpleName ?: "unknown error",
                        )
                    )
                }
            }
        }.awaitAll()

        StreamsResult(
            streams = outcomes.flatMap { it.streams },
            errors = outcomes.mapNotNull { it.error },
        )
    }
}

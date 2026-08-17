package fr.vinarnt.animu.finder.compose.repository.extractor

import fr.vinarnt.animu.finder.compose.model.StreamSource

data class ProviderError(
    val providerId: String,
    val providerName: String,
    val message: String,
)

data class StreamsResult(
    val streams: List<StreamSource>,
    val errors: List<ProviderError>,
)

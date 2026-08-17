package fr.vinarnt.animu.finder.compose.repository.extractor

actual fun provideExtractorHttpClient(): ExtractorHttpClient =
    ExtractorHttpClient(createExtractorHttpClient())

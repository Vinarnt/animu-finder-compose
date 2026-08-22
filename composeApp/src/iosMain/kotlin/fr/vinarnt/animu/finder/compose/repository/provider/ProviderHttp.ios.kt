package fr.vinarnt.animu.finder.compose.repository.provider

actual fun provideProviderHttpClient(): ProviderHttpClient =
    ProviderHttpClient(createProviderHttpClient())

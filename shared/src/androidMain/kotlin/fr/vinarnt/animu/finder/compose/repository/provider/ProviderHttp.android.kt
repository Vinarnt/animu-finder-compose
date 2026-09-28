package fr.vinarnt.animu.finder.compose.repository.provider

import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore

actual fun provideProviderHttpClient(clearances: CloudflareClearanceStore): ProviderHttpClient =
    ProviderHttpClient(createProviderHttpClient(), clearances)

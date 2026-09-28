package fr.vinarnt.animu.finder.compose.repository.provider

import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore

expect fun provideProviderHttpClient(clearances: CloudflareClearanceStore): ProviderHttpClient

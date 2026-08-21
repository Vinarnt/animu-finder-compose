package fr.vinarnt.animu.finder.compose.di

import io.ktor.client.HttpClientConfig

expect fun HttpClientConfig<*>.configureJikanCache()

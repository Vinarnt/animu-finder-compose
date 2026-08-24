package fr.vinarnt.animu.finder.compose.di

import io.github.santimattius.persistent.cache.CacheConfig
import io.github.santimattius.persistent.cache.installPersistentCache
import io.ktor.client.HttpClientConfig

actual fun HttpClientConfig<*>.configureJikanCache() {
    installPersistentCache(
        CacheConfig(
            enabled = true,
            cacheDirectory = "jikan_cache",
            maxCacheSize = 50L * 1024 * 1024,
            cacheTtl = 0,
            isShared = true,
            isPublic = true,
        )
    )
}

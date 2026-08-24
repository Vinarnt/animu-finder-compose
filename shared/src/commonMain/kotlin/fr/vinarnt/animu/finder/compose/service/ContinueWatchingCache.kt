package fr.vinarnt.animu.finder.compose.service

import fr.vinarnt.animu.finder.compose.model.ContinueWatchingAnime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val RESUME_ANIME_CACHE_KEY = "resumeAnimeCache"

class ContinueWatchingCache(private val settingManager: SettingManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    suspend fun get(animeId: Int): ContinueWatchingAnime? = mutex.withLock {
        read()[animeId]
    }

    suspend fun put(animeId: Int, anime: ContinueWatchingAnime) = mutex.withLock {
        write(read() + (animeId to anime))
    }

    suspend fun prune(validIds: Set<Int>) = mutex.withLock {
        write(read().filterKeys { it in validIds })
    }

    private suspend fun read(): Map<Int, ContinueWatchingAnime> {
        val raw = settingManager.settings.getStringOrNull(RESUME_ANIME_CACHE_KEY) ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<Int, ContinueWatchingAnime>>(raw) }
            .getOrElse { emptyMap() }
    }

    private suspend fun write(map: Map<Int, ContinueWatchingAnime>) {
        settingManager.settings.putString(RESUME_ANIME_CACHE_KEY, json.encodeToString(map))
    }
}

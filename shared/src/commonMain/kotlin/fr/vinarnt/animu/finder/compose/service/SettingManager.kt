package fr.vinarnt.animu.finder.compose.service

import androidx.compose.ui.text.intl.Locale
import cafe.adriel.lyricist.LanguageTag
import com.russhwolf.settings.coroutines.FlowSettings
import fr.vinarnt.animu.finder.compose.model.CloudflareClearance
import fr.vinarnt.animu.finder.compose.model.ContinueWatchingEntry
import fr.vinarnt.animu.finder.compose.model.EpisodeDisplay
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingManager(val settings: FlowSettings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getTheme() = settings.getStringFlow("theme", Theme.AUTO.name)
        .transform { emit(Theme.valueOf(it)) }

    suspend fun setTheme(theme: Theme) = settings.putString("theme", theme.name)

    fun getLocale() = settings.getStringFlow("locale", Locale.current.language)

    suspend fun setLocale(locale: LanguageTag) = settings.putString("locale", locale)

    fun getDefaultQuality() = settings.getStringFlow("defaultQuality", "Auto")

    suspend fun setDefaultQuality(quality: String) = settings.putString("defaultQuality", quality)

    fun getPreferredSubtitles() = settings.getStringFlow("preferredSubtitles", "English")

    suspend fun setPreferredSubtitles(subtitles: String) = settings.putString("preferredSubtitles", subtitles)

    fun getAutoplayNext() = settings.getBooleanFlow("autoplayNext", true)

    suspend fun setAutoplayNext(enabled: Boolean) = settings.putBoolean("autoplayNext", enabled)

    fun getEpisodeDisplay() = settings.getStringFlow("episodeDisplay", EpisodeDisplay.POSTER.name)
        .transform { emit(EpisodeDisplay.valueOf(it)) }

    suspend fun setEpisodeDisplay(display: EpisodeDisplay) = settings.putString("episodeDisplay", display.name)

    suspend fun getSubtitlePosition(): SubtitlePosition =
        decodeSubtitlePosition(settings.getStringFlow("subtitlePosition", "{}").first())

    suspend fun setSubtitlePosition(position: SubtitlePosition) =
        settings.putString("subtitlePosition", json.encodeToString(position))

    /**
     * Cloudflare clearances keyed by host. Each entry holds the `cf_clearance` cookie
     * and the User-Agent that solved the challenge (Cloudflare binds the two).
     * See [fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore].
     */
    fun getCloudflareClearances(): Flow<Map<String, CloudflareClearance>> =
        settings.getStringFlow("cloudflareClearances", "{}")
            .map { decodeClearances(it) }

    suspend fun saveCloudflareClearance(host: String, clearance: CloudflareClearance) {
        val current = getCloudflareClearances().first()
        settings.putString(
            "cloudflareClearances",
            json.encodeToString(current + (host.lowercase() to clearance)),
        )
    }

    suspend fun removeCloudflareClearance(host: String) {
        val current = getCloudflareClearances().first()
        settings.putString(
            "cloudflareClearances",
            json.encodeToString(current - host.lowercase()),
        )
    }

    fun getWatchHistory(): Flow<List<ContinueWatchingEntry>> =
        settings.getStringFlow("watchHistory", "[]")
            .map { decode(it) }

    suspend fun addContinueWatching(entry: ContinueWatchingEntry) {
        val current = getWatchHistory().first()
        val updated = (listOf(entry) + current.filterNot { it.animeId == entry.animeId }).take(10)
        settings.putString("watchHistory", json.encodeToString(updated))
    }

    fun getMyList(): Flow<List<Int>> =
        settings.getStringFlow("myList", "[]")
            .map { decodeIntList(it) }

    suspend fun toggleMyList(animeId: Int) {
        val current = getMyList().first()
        val updated = if (animeId in current) current - animeId else current + animeId
        settings.putString("myList", json.encodeToString(updated))
    }

    private fun decode(value: String): List<ContinueWatchingEntry> =
        runCatching { json.decodeFromString<List<ContinueWatchingEntry>>(value) }
            .getOrElse { emptyList() }

    private fun decodeIntList(value: String): List<Int> =
        runCatching { json.decodeFromString<List<Int>>(value) }
            .getOrElse { emptyList() }

    private fun decodeClearances(value: String): Map<String, CloudflareClearance> =
        runCatching { json.decodeFromString<Map<String, CloudflareClearance>>(value) }
            .getOrElse { emptyMap() }

    private fun decodeSubtitlePosition(value: String): SubtitlePosition =
        runCatching { json.decodeFromString<SubtitlePosition>(value) }
            .getOrElse { SubtitlePosition() }
}

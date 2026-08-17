package fr.vinarnt.animu.finder.compose.repository.extractor

import co.touchlab.kermit.Logger
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitleTrack
import fr.vinarnt.animu.finder.compose.model.SubtitleType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class VoirAnimeExtractor(http: ExtractorHttpClient) : BaseExtractor(http) {

    override val providerId = "voiranime"
    override val providerName = "VoirAnime"

    private val base = "https://voir-anime.to"

    private data class Resolved(val url: String, val referer: String)

    private data class Candidate(val slug: String, val title: String, val episodeCount: Int)

    override suspend fun extractStreams(query: EpisodeSearchQuery): List<StreamSource> = coroutineScope {
        val slugs = resolveSlugs(query)
        if (slugs.isEmpty()) return@coroutineScope emptyList()

        slugs.map { (slug, isDub) ->
            async {
                val resolved = resolveStreamUrl(slug, query.episode.absolute) ?: return@async null
                StreamSource(
                    providerId = providerId,
                    url = resolved.url,
                    quality = null,
                    isM3U8 = resolved.url.contains(".m3u8"),
                    headers = mapOf(
                        "Referer" to resolved.referer,
                        "User-Agent" to DEFAULT_USER_AGENT,
                    ),
                    dub = if (isDub) "fr" else null,
                    subtitles = if (isDub) {
                        emptyList()
                    } else {
                        listOf(SubtitleTrack("fr", SubtitleType.Hard))
                    },
                    matchScore = 1f,
                )
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun resolveSlugs(query: EpisodeSearchQuery): List<Pair<String, Boolean>> =
        coroutineScope {
            val searchDeferred = async { searchCandidates(query) }
            val probeDeferred = async { slugifyProbe(query) }
            val candidates = buildList {
                addAll(searchDeferred.await())
                probeDeferred.await()?.let { add(it) }
            }

            val results = mutableListOf<Pair<String, Boolean>>()
            candidates.filter { !it.slug.endsWith("-vf") }.maxByOrNull { score(it, query) }?.let {
                results.add(it.slug to false)
            }
            candidates.filter { it.slug.endsWith("-vf") }.maxByOrNull { score(it, query) }?.let {
                results.add(it.slug to true)
            }
            results
        }

    private suspend fun searchCandidates(query: EpisodeSearchQuery): List<Candidate> {
        val text = try {
            http.getText("$base/?post_type=wp-manga&s=${encode(query.animeTitle)}", headers())
        } catch (e: Exception) {
            Logger.w("VoirAnime search failed: ${e.message}", e)
            return emptyList()
        }

        val regex = Regex(
            """<h3 class="h4"><a href="[^"]*/anime/([a-z0-9-]+)/"[^>]*>([^<]+)</a></h3>[\s\S]*?<span class="font-meta chapter"><a href="[^"]*">(\d+)</a></span>"""
        )
        return regex.findAll(text).mapNotNull { match ->
            val slug = match.groupValues[1]
            if (slug == "feed") return@mapNotNull null
            Candidate(
                slug = slug,
                title = match.groupValues[2].trim(),
                episodeCount = match.groupValues[3].toIntOrNull() ?: 0,
            )
        }.toList()
    }

    private suspend fun slugifyProbe(query: EpisodeSearchQuery): Candidate? {
        val slug = slugify(query.animeTitle)
        if (slug.isEmpty()) return null
        return try {
            http.getText("$base/anime/$slug/", headers())
            Candidate(slug, query.animeTitle, 0)
        } catch (e: Exception) {
            Logger.w("VoirAnime slugify probe $slug failed: ${e.message}", e)
            null
        }
    }

    private fun score(candidate: Candidate, query: EpisodeSearchQuery): Float {
        var value = bestTitleScore(query, candidate.title)
        if (candidate.episodeCount >= query.episode.absolute) value *= 1.2f
        val total = query.totalEpisodes
        if (total != null && candidate.episodeCount == total) value *= 1.2f
        return value
    }

    private fun slugify(title: String): String =
        title.lowercase()
            .replace(Regex("[;:'!.,()\\[\\]&+$]"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')

    private suspend fun resolveStreamUrl(slug: String, episodeNumber: Int): Resolved? {
        val suffix = if (slug.endsWith("-vf")) "vf" else "vostfr"
        val episodeBase = slug.removeSuffix("-vf").removeSuffix("-vostfr")
        val numbers = listOf(
            episodeNumber.toString().padStart(2, '0'),
            episodeNumber.toString(),
        ).distinct()

        val constructedUrl = "$base/anime/$slug/$episodeBase-${numbers.first()}-$suffix/"
        var episodeHtml = fetchPageWithPlayer(constructedUrl)

        if (episodeHtml == null) {
            episodeHtml = scanAnimePageForEpisode(slug, numbers, suffix)
        }
        if (episodeHtml == null) return null

        val iframes = extractIframes(episodeHtml)
            // Only fetch embeds we can actually resolve; voe/streamtape/... require
            // JavaScript and would otherwise block for the full HTTP timeout.
            .filter { EmbedResolver.canResolve(it) }
        for (iframe in iframes) {
            try {
                val embedHtml = http.getText(iframe, headers())
                val stream = EmbedResolver.extract(iframe, embedHtml)
                if (stream != null) {
                    return Resolved(stream, refererOf(iframe))
                }
            } catch (e: Exception) {
                Logger.w("VoirAnime embed $iframe failed: ${e.message}", e)
            }
        }
        return null
    }

    private suspend fun fetchPageWithPlayer(url: String): String? {
        return try {
            val html = http.getText(url, headers())
            if (extractIframes(html).isEmpty()) null else html
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun scanAnimePageForEpisode(slug: String, numbers: List<String>, suffix: String): String? {
        val animeHtml = try {
            http.getText("$base/anime/$slug/", headers())
        } catch (e: Exception) {
            return null
        }
        for (number in numbers) {
            val href = Regex("""href="([^"]*/anime/$slug/[^"]*-$number-$suffix/")""")
                .find(animeHtml)?.groupValues?.get(1)
                ?: Regex("""href="([^"]*/anime/$slug/[^"]*-$number-[a-z]+/")""")
                    .find(animeHtml)?.groupValues?.get(1)
            if (href != null) {
                val url = href.replace("\\/", "/")
                val full = if (url.startsWith("http")) url else "$base$url"
                val html = fetchPageWithPlayer(full)
                if (html != null) return html
            }
        }
        return null
    }

    private fun extractIframes(html: String): List<String> =
        Regex("""<iframe[^>]+src=["']([^"']+)["']""")
            .findAll(html)
            .map { it.groupValues[1].replace("\\/", "/") }
            .toList()

    private fun refererOf(iframe: String): String =
        iframe.substringBeforeLast("/") + "/"

    private fun headers(referer: String = base) = mapOf(
        "User-Agent" to DEFAULT_USER_AGENT,
        "Referer" to referer,
    )
}

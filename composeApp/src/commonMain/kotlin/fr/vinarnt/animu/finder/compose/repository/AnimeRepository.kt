package fr.vinarnt.animu.finder.compose.repository

import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.JikanClient
import fr.vinarnt.jikan4k.apis.AnimeApi
import fr.vinarnt.jikan4k.models.GetAnime200Response
import fr.vinarnt.jikan4k.models.GetAnimeById200ResponseData
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodes200Response
import fr.vinarnt.jikan4k.models.GetAnimeByIdEpisodesByEpisodeId200ResponseData

class AnimeRepository(private val jikan: JikanClient) {

    suspend fun searchAnimes(
        page: Int = 1,
        limit: Int = 25,
        query: String? = null,
        type: AnimeApi.TypeGetAnime? = null,
        status: AnimeApi.StatusGetAnime? = null,
        rating: AnimeApi.RatingGetAnime? = null,
        minScore: Double? = null,
        maxScore: Double? = null,
        genres: Set<AnimeGenre> = emptySet()
    ): GetAnime200Response = jikan.animes.getAnime(
        q = query?.takeIf { it.isNotBlank() },
        page = page,
        limit = limit,
        type = type?.let { listOf(it) },
        status = status,
        rating = rating?.let { listOf(it) },
        minScore = minScore,
        maxScore = maxScore,
        genres = genres.joinToString(",") { it.id.toString() }.takeIf { it.isNotEmpty() },
        orderBy = AnimeApi.OrderByGetAnime.SCORE,
        sort = AnimeApi.SortGetAnime.DESC
    ).body()

    suspend fun getAnimeById(id: Int): GetAnimeById200ResponseData =
        jikan.animes.getAnimeById(id).body().data

    suspend fun getAnimeEpisodes(id: Int, page: Int = 1): GetAnimeByIdEpisodes200Response =
        jikan.animes.getAnimeByIdEpisodes(id, page = page).body()

    suspend fun getAnimeEpisodeById(id: Int, episode: Int): GetAnimeByIdEpisodesByEpisodeId200ResponseData =
        jikan.animes.getAnimeByIdEpisodesByEpisodeId(id, episode).body().data
}
package fr.vinarnt.animu.finder.compose.repository

import fr.vinarnt.jikan4k.JikanClient
import fr.vinarnt.jikan4k.models.Anime
import fr.vinarnt.jikan4k.models.AnimeEpisodes
import fr.vinarnt.jikan4k.models.AnimeSearch
import fr.vinarnt.jikan4k.models.AnimeSearchQueryOrderby
import fr.vinarnt.jikan4k.models.AnimeSearchQueryRating
import fr.vinarnt.jikan4k.models.AnimeSearchQueryStatus
import fr.vinarnt.animu.finder.compose.model.AnimeGenre
import fr.vinarnt.jikan4k.models.AnimeTypes
import fr.vinarnt.jikan4k.models.SearchQuerySort

class AnimeRepository(private val jikan: JikanClient) {

    suspend fun searchAnimes(
        page: Int = 1,
        limit: Int = 25,
        query: String? = null,
        type: AnimeTypes? = null,
        status: AnimeSearchQueryStatus? = null,
        rating: AnimeSearchQueryRating? = null,
        minScore: Double? = null,
        maxScore: Double? = null,
        genres: Set<AnimeGenre> = emptySet()
    ): AnimeSearch = jikan.animes.getAnimeSearch(
        page = page,
        limit = limit,
        q = query?.takeIf { it.isNotBlank() },
        type = type,
        status = status,
        rating = rating,
        minScore = minScore,
        maxScore = maxScore,
        genres = genres.joinToString(",") { it.id.toString() }.takeIf { it.isNotEmpty() },
        orderBy = AnimeSearchQueryOrderby.SCORE,
        sort = SearchQuerySort.DESC
    ).body()

    suspend fun getAnimeById(id: Int): Anime =
        jikan.animes.getAnimeById(id).body().data ?: throw Exception("Anime with id $id not found")

    suspend fun getAnimeEpisodes(id: Int, page: Int = 1): AnimeEpisodes =
        jikan.animes.getAnimeEpisodes(id, page = page).body()
}
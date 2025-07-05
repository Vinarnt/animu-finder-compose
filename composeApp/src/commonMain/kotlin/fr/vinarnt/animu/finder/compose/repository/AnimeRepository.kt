package fr.vinarnt.animu.finder.compose.repository

import fr.vinarnt.jikan4k.JikanClient
import fr.vinarnt.jikan4k.models.Anime
import fr.vinarnt.jikan4k.models.AnimeSearch


class AnimeRepository(private val jikan: JikanClient) {

    suspend fun getTopAnimes(page: Int = 1): AnimeSearch = jikan.tops.getTopAnime(page = page).body()

    suspend fun getAnimeById(id: Int): Anime =
        jikan.animes.getAnimeById(id).body().data ?: throw Exception("Anime with id $id not found")
}

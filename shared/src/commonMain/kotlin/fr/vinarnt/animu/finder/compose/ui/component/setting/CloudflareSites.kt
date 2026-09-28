package fr.vinarnt.animu.finder.compose.ui.component.setting

/** A provider site that may present a Cloudflare challenge, offered in Settings. */
data class CloudflareSite(
    val label: String,
    val host: String,
) {
    val url: String get() = "https://$host/"
}

/** The provider hosts the app talks to; capture a clearance for the ones that challenge. */
val cloudflareSites: List<CloudflareSite> = listOf(
    CloudflareSite("Nakanime", "nakanime.tv"),
    CloudflareSite("Anime-Sama", "anime-sama.to"),
    CloudflareSite("VoirAnime", "voir-anime.to"),
    CloudflareSite("AniNeko", "anineko.to"),
    CloudflareSite("AnimePahe", "animepahe.pw"),
    CloudflareSite("AnimeYa", "animeya.cc"),
    CloudflareSite("AnimeHeaven", "animeheaven.me"),
    CloudflareSite("AnimeParadise", "animeparadise.moe"),
    CloudflareSite("KickAssAnime", "kaa.lt"),
)

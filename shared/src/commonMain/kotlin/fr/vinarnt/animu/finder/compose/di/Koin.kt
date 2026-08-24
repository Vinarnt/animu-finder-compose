package fr.vinarnt.animu.finder.compose.di

import fr.vinarnt.animu.finder.compose.logger.KermitKoinLogger
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.repository.SubtitleRepository
import fr.vinarnt.animu.finder.compose.repository.provider.AnimeHeavenProvider
import fr.vinarnt.animu.finder.compose.repository.provider.AniNekoProvider
import fr.vinarnt.animu.finder.compose.repository.provider.AnimePaheProvider
import fr.vinarnt.animu.finder.compose.repository.provider.AnimeParadiseProvider
import fr.vinarnt.animu.finder.compose.repository.provider.AnimeSamaProvider
import fr.vinarnt.animu.finder.compose.repository.provider.AnimeYaProvider
import fr.vinarnt.animu.finder.compose.repository.provider.KickAssAnimeProvider
import fr.vinarnt.animu.finder.compose.repository.provider.ProviderHttpClient
import fr.vinarnt.animu.finder.compose.repository.provider.StreamRepository
import fr.vinarnt.animu.finder.compose.repository.provider.StreamingProvider
import fr.vinarnt.animu.finder.compose.repository.provider.VoirAnimeProvider
import fr.vinarnt.animu.finder.compose.repository.provider.provideProviderHttpClient
import fr.vinarnt.animu.finder.compose.service.ContinueWatchingCache
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
import fr.vinarnt.animu.finder.compose.viewmodel.EpisodeDetailViewModel
import fr.vinarnt.jikan4k.JikanClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger


private val commonModule = module {
    single<JikanClient> {
        JikanClient(
            baseUrl = "https://api.tenrai.org/v1",
            httpClientConfig = { config ->
                with(config) {
                    install(Logging) {
                        logger = object : KtorLogger {
                            override fun log(message: String) {
                                KermitLogger.v(message, null, "Ktor Request")
                            }
                        }
                        level = LogLevel.INFO
                    }
                    configureJikanCache()
                }
            }
        )
    }

    viewModelOf(::AnimeListViewModel)
    viewModelOf(::AnimeDetailViewModel)
    viewModelOf(::EpisodeDetailViewModel)

    singleOf(::AnimeRepository)
    singleOf(::SubtitleRepository)
    singleOf(::StreamRepository)
    singleOf(::ContinueWatchingCache)

    single { provideProviderHttpClient() }

    single<List<StreamingProvider>> {
        val http: ProviderHttpClient = get()
        listOf(
            AniNekoProvider(http),
            VoirAnimeProvider(http),
            AnimeSamaProvider(http),
            AnimePaheProvider(http),
            AnimeYaProvider(http),
            AnimeHeavenProvider(http),
            AnimeParadiseProvider(http),
            KickAssAnimeProvider(http),
        )
    }

}

val appModule = module {
    includes(commonModule + platformModule)
}

fun koinAppDeclaration(koinAppDeclaration: KoinAppDeclaration): KoinAppDeclaration = {
    logger(KermitKoinLogger(KermitLogger.withTag("koin")))
    modules(appModule)
    koinAppDeclaration()
}

internal expect val platformModule: Module

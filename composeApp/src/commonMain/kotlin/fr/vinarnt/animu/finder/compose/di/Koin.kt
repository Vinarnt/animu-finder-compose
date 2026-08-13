package fr.vinarnt.animu.finder.compose.di

import fr.vinarnt.animu.finder.compose.logger.KermitKoinLogger
import fr.vinarnt.animu.finder.compose.repository.AnimeRepository
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeDetailViewModel
import fr.vinarnt.animu.finder.compose.viewmodel.AnimeListViewModel
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
                }
            }
        )
    }

    viewModelOf(::AnimeListViewModel)
    viewModelOf(::AnimeDetailViewModel)

    singleOf(::AnimeRepository)
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

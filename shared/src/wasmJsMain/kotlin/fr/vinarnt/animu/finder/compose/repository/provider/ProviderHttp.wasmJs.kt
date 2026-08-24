package fr.vinarnt.animu.finder.compose.repository.provider

import io.ktor.client.HttpClient
import io.ktor.http.encodeURLParameter

actual fun provideProviderHttpClient(): ProviderHttpClient =
    CorsProviderHttpClient(createProviderHttpClient(), CORS_PROXIES)

class CorsProviderHttpClient(
    client: HttpClient,
    private val corsProxies: List<(String) -> String>,
) : ProviderHttpClient(client) {

    private var nextProxyIndex = 0

    override suspend fun execute(url: String, headers: Map<String, String>, body: String?): String {
        if (corsProxies.isEmpty()) return super.execute(url, headers, body)

        val start = nextProxyIndex
        nextProxyIndex = (nextProxyIndex + 1) % corsProxies.size

        var lastError: Throwable? = null
        for (i in corsProxies.indices) {
            val proxy = corsProxies[(start + i) % corsProxies.size]
            try {
                return super.execute(proxy(url), headers, body)
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("All CORS proxies failed")
    }
}

val CORS_PROXIES: List<(String) -> String> = listOf(
    { url -> "https://api.allorigins.win/raw?url=${url.encodeURLParameter()}" },
    { url -> "https://api.codetabs.com/v1/proxy?quest=${url.encodeURLParameter()}" },
    { url -> "https://cors.isomorphic-git.org/$url" },
)
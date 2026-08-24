package fr.vinarnt.animu.finder.compose.repository.provider

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlin.time.TimeSource

actual fun provideProviderHttpClient(): ProviderHttpClient =
    CorsProviderHttpClient(createProviderHttpClient(), CORS_PROXIES)

class CorsProviderHttpClient(
    client: HttpClient,
    private val corsProxies: List<(String) -> String>,
) : ProviderHttpClient(client) {

    private var rankedProxies: List<(String) -> String>? = null

    override suspend fun execute(url: String, headers: Map<String, String>, body: String?): String {
        val proxies = rankedProxies ?: rankByLatency().also { rankedProxies = it }

        var lastError: Throwable? = null
        for (proxy in proxies) {
            try {
                return super.execute(proxy(url), headers, body)
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("All CORS proxies failed")
    }

    private suspend fun rankByLatency(): List<(String) -> String> {
        val probe = "https://example.com/"
        val results = corsProxies.map { proxy ->
            val mark = TimeSource.Monotonic.markNow()
            val ok = try {
                client.get(proxy(probe)).status.isSuccess()
            } catch (e: Exception) {
                false
            }
            Triple(proxy, mark.elapsedNow().inWholeMilliseconds, ok)
        }
        return results
            .sortedWith(compareBy({ !it.third }, { it.second }))
            .map { it.first }
    }
}

val CORS_PROXIES: List<(String) -> String> = listOf(
    { url -> "https://api.allorigins.win/raw?url=${url.encodeURLParameter()}" },
    { url -> "https://api.codetabs.com/v1/proxy?quest=${url.encodeURLParameter()}" },
    { url -> "https://cors.isomorphic-git.org/$url" },
)
package fr.vinarnt.animu.finder.compose.repository.extractor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlin.concurrent.Volatile
import kotlin.time.TimeSource

class CloudflareChallengeException(val url: String) :
    Exception("Blocked by anti-bot challenge: $url")

const val DEFAULT_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

fun createExtractorHttpClient(): HttpClient = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 10_000
        socketTimeoutMillis = 10_000
    }
    install(HttpRedirect) {
        checkHttpMethod = false
        allowHttpsDowngrade = false
    }
    defaultRequest {
        header(HttpHeaders.UserAgent, DEFAULT_USER_AGENT)
        header(HttpHeaders.Accept, "*/*")
        header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
    }
}

class ExtractorHttpClient(
    private val client: HttpClient,
    private val corsProxies: List<(String) -> String> = emptyList(),
) {

    @Volatile
    private var rankedProxies: List<(String) -> String>? = null

    suspend fun getText(url: String, headers: Map<String, String> = emptyMap()): String =
        fetch(url, headers, null)

    suspend fun postForm(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
    ): String {
        val body = form.entries.joinToString("&") { (key, value) ->
            "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
        }
        return fetch(url, headers, body)
    }

    private suspend fun fetch(url: String, headers: Map<String, String>, body: String?): String {
        if (corsProxies.isEmpty()) {
            return request(url, headers, body)
        }

        val proxies = rankedProxies ?: rankByLatency().also { rankedProxies = it }

        var lastError: Throwable? = null
        for (proxy in proxies) {
            try {
                return request(proxy(url), headers, body)
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("All CORS proxies failed")
    }

    private suspend fun request(url: String, headers: Map<String, String>, body: String?): String {
        val response = if (body == null) {
            client.get(url) {
                headers.forEach { (key, value) -> header(key, value) }
            }
        } else {
            client.post(url) {
                headers.forEach { (key, value) -> header(key, value) }
                setBody(TextContent(body, ContentType.Application.FormUrlEncoded))
            }
        }

        check(response.status.isSuccess()) { "HTTP ${response.status.value} for $url" }

        val text = response.bodyAsText()
        if (isChallengePage(text)) throw CloudflareChallengeException(url)
        return text
    }

    private fun isChallengePage(body: String): Boolean {
        val lower = body.lowercase()
        return (lower.contains("window.location.replace") && lower.contains("ch=1")) ||
            lower.contains("_gs_challenge") ||
            (lower.contains("just a moment") && lower.contains("cf-ray"))
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

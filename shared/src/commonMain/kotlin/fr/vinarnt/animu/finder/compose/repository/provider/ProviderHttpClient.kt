package fr.vinarnt.animu.finder.compose.repository.provider

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
class CloudflareChallengeException(val url: String) :
    Exception("Blocked by anti-bot challenge: $url")

const val DEFAULT_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

fun createProviderHttpClient(): HttpClient = HttpClient {
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

open class ProviderHttpClient(
    protected val client: HttpClient,
) {
    suspend fun getText(url: String, headers: Map<String, String> = emptyMap()): String =
        execute(url, headers, null)

    suspend fun postForm(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
    ): String {
        val body = form.entries.joinToString("&") { (key, value) ->
            "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
        }
        return execute(url, headers, body)
    }

    suspend fun postJson(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
    ): String =
        execute(url, headers + (HttpHeaders.ContentType to ContentType.Application.Json.toString()), jsonBody)

    protected open suspend fun execute(url: String, headers: Map<String, String>, body: String?): String {
        val response = if (body == null) {
            client.get(url) {
                headers.forEach { (key, value) -> header(key, value) }
            }
        } else {
            client.post(url) {
                headers.forEach { (key, value) -> header(key, value) }
                val contentType = headers[HttpHeaders.ContentType]
                    ?.let { runCatching { ContentType.parse(it) }.getOrNull() }
                    ?: ContentType.Application.FormUrlEncoded
                setBody(TextContent(body, contentType))
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
}

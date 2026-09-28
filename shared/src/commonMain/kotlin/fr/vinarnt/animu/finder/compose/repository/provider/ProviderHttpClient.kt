package fr.vinarnt.animu.finder.compose.repository.provider

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.util.appendIfNameAbsent
import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore
import fr.vinarnt.animu.finder.compose.service.isCloudflareProtected

class CloudflareChallengeException(val url: String) :
    Exception("Blocked by anti-bot challenge: $url")

class HttpStatusException(url: String, val status: Int) :
    Exception("HTTP $status for $url")

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
        // appendIfNameAbsent, NOT header(): Ktor's DefaultRequest runs after the request is
        // built, so header() appends this default *after* any request-level value and the two
        // get comma-joined. That broke Cloudflare: the clearance UA became
        // "<clearance UA>,<chrome UA>", which the challenge rejects. With appendIfNameAbsent a
        // request-level User-Agent (the clearance one) wins outright.
        headers.appendIfNameAbsent(HttpHeaders.UserAgent, DEFAULT_USER_AGENT)
        headers.appendIfNameAbsent(HttpHeaders.Accept, "*/*")
        headers.appendIfNameAbsent(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
    }
}

open class ProviderHttpClient(
    protected val client: HttpClient,
    private val cloudflare: CloudflareClearanceStore? = null,
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

    /**
     * Raw response body. Needed by APIs (e.g. Nakanime) that XOR-obfuscate their
     * payloads: decoding the body as text first would replace the high bytes with
     * U+FFFD and make the payload unrecoverable.
     */
    suspend fun getBytes(url: String, headers: Map<String, String> = emptyMap()): ByteArray =
        executeBytes(url, headers, null)

    suspend fun postJsonBytes(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
    ): ByteArray =
        executeBytes(url, headers + (HttpHeaders.ContentType to ContentType.Application.Json.toString()), jsonBody)

    protected suspend fun execute(url: String, headers: Map<String, String>, body: String?): String {
        val text = performAuthorized(url, headers, body).bodyAsText()
        if (isChallengePage(text)) throw CloudflareChallengeException(url)
        return text
    }

    protected suspend fun executeBytes(url: String, headers: Map<String, String>, body: String?): ByteArray =
        performAuthorized(url, headers, body).bodyAsBytes()

    /**
     * Adds any stored Cloudflare clearance for the request host before delegating to
     * [perform]. Done here (not in [perform]) so the headers are computed from the real
     * target URL even when wasm rewrites it through a CORS proxy.
     */
    private suspend fun performAuthorized(
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): HttpResponse {
        val extra = cloudflare?.headersFor(url).orEmpty()
        return try {
            perform(url, if (extra.isEmpty()) headers else headers + extra, body)
        } catch (e: HttpStatusException) {
            // Blocked with no clearance (or a stale one): ask the UI to solve the
            // challenge in the WebView, then retry once with the fresh cookie.
            if ((e.status == 403 || e.status == 503) && cloudflare != null) {
                val host = cloudflare.hostOf(url)
                val solved = host
                    ?.takeIf { isCloudflareProtected(it) }
                    ?.let { cloudflare.requestClearance(it, force = true) }
                if (solved != null && solved.cookie.isNotBlank()) {
                    val retry = cloudflare.headersFor(url)
                    return perform(url, if (retry.isEmpty()) headers else headers + retry, body)
                }
            }
            throw e
        }
    }

    /**
     * Sends the request and fails on a non-2xx status. Overridden on wasmJs so every
     * call (text or bytes) goes through the CORS proxies when a direct request fails.
     */
    protected open suspend fun perform(
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): HttpResponse {
        val response = send(url, headers, body)
        if (!response.status.isSuccess()) throw HttpStatusException(url, response.status.value)
        return response
    }

    private suspend fun send(
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): HttpResponse = if (body == null) {
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

    private fun isChallengePage(body: String): Boolean {
        val lower = body.lowercase()
        return (lower.contains("window.location.replace") && lower.contains("ch=1")) ||
                lower.contains("_gs_challenge") ||
                (lower.contains("just a moment") && lower.contains("cf-ray"))
    }
}

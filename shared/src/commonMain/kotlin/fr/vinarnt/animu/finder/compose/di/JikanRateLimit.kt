package fr.vinarnt.animu.finder.compose.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val MinRequestSpacing = 200.milliseconds
private const val MaxRetriesOn429 = 3

/**
 * Stops Jikan/Tenrai requests from tripping the API's burst limit.
 *
 * The API answers 429 when roughly four requests are in flight at once: 8 in parallel gave
 * 4x200 + 4x429, 6 in parallel gave 4x200 + 2x429, and the same requests spaced out all
 * succeeded. The home screen fires six queries at once on a cold start and its shelves turn
 * failures into empty lists, so two rows could come back empty.
 *
 * The gate sits on the client, so callers do not have to know about the limit: one request in
 * flight, with a gap between starts. [HttpRequestRetry] picks up a 429 from anywhere else.
 */
private val JikanRateLimit =
    createClientPlugin("JikanRateLimit") {
        val mutex = Mutex()
        var lastStart: TimeMark? = null

        on(Send) { request ->
            mutex.withLock {
                lastStart?.let { previous ->
                    val elapsed = previous.elapsedNow()
                    if (elapsed < MinRequestSpacing) delay(MinRequestSpacing - elapsed)
                }
                // Marked before the send, so the gap is measured between request starts.
                lastStart = TimeSource.Monotonic.markNow()
                proceed(request)
            }
        }
    }

/**
 * Adds the burst gate, a request timeout and a 429 retry to the Jikan client.
 *
 * Retry is installed before the gate, so a retry goes through the gate again rather than holding
 * it during the backoff.
 *
 * jikan4k sets no timeout and the gate allows one request at a time, so without one a stalled
 * request would block every later call. The timeout also bounds a retry chain.
 */
fun HttpClientConfig<*>.configureJikanRateLimit() {
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
        socketTimeoutMillis = 20_000
    }
    install(HttpRequestRetry) {
        retryIf(maxRetries = MaxRetriesOn429) { _, response ->
            response.status == HttpStatusCode.TooManyRequests
        }
        exponentialDelay(
            baseDelayMs = 500,
            maxDelayMs = 4_000,
            randomizationMs = 250,
            // The API sends Retry-After with some limiter responses.
            respectRetryAfterHeader = true,
        )
    }
    install(JikanRateLimit)
}

package fr.vinarnt.animu.finder.compose.ui.component.setting

import dev.nucleusframework.webview.web.IWebView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Reads the WebView engine's **real** User-Agent via JS.
 *
 * Cloudflare binds `cf_clearance` to the User-Agent that passed the challenge, so the UA we
 * store (and later replay) must be the one the engine actually used. Spoofing a desktop
 * Chrome UA on a WebKit engine (WebKit2GTK on desktop, WKWebView on iOS, Android WebView)
 * makes Turnstile's fingerprint check fail, so the capture path never pins a UA any more —
 * it reads the engine's own here.
 *
 * Returns null if the callback does not arrive within [timeoutMs] (the JS bridge can be
 * flaky, and callers fall back to [platformUserAgent]).
 */
internal suspend fun IWebView.userAgentOrNull(timeoutMs: Long = 3_000): String? =
    withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { continuation ->
            runCatching {
                evaluateJavaScript("navigator.userAgent") { result ->
                    continuation.resume(result.toJsStringOrNull())
                }
            }.onFailure {
                continuation.resume(null)
            }
        }
    }

/**
 * The JS bridge hands the value back already serialized, so a string arrives wrapped in
 * JSON quotes (`"Mozilla/5.0 …"`). Cloudflare binds the clearance to the exact UA, so the
 * quotes must be stripped: a quoted `User-Agent` header mismatches the cookie and the
 * retry 403s again.
 */
private fun String.toJsStringOrNull(): String? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null
    val body =
        if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }
    return body.replace("\\\"", "\"").replace("\\\\", "\\").takeIf { it.isNotEmpty() }
}

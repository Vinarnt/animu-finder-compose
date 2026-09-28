package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewState
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.CloudflareClearance
import fr.vinarnt.animu.finder.compose.repository.provider.DEFAULT_USER_AGENT
import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore
import fr.vinarnt.animu.finder.compose.ui.theme.CornerRadius
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Mounts the automatic Cloudflare challenge solver: when a provider is blocked and no
 * clearance is stored, [CloudflareClearanceStore.solveRequests] fires and this opens the
 * host in the in-app WebView until `cf_clearance` appears, then hands it back so the
 * provider can retry. Silent on platforms without an in-app WebView (wasm).
 */
@Composable
fun CloudflareChallengeHost() {
    val store: CloudflareClearanceStore = koinInject()
    var host by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(store) {
        store.solveRequests.collect { host = it }
    }

    host?.let { target ->
        CloudflareChallengeOverlay(host = target, onFinished = { host = null })
    }
}

@Composable
private fun CloudflareChallengeOverlay(
    host: String,
    onFinished: () -> Unit,
) {
    val store: CloudflareClearanceStore = koinInject()
    val scope = rememberCoroutineScope()
    val s = strings.settings.cloudflare
    val url = "https://$host/"

    if (!supportsInAppCookieCapture || !isInAppWebViewReady) {
        // No in-app browser (wasm), or the engine isn't up yet: don't bootstrap/download
        // it implicitly. Unblock the caller so it fails as before.
        LaunchedEffect(host) {
            store.completeSolve(host, null)
            onFinished()
        }
        return
    }

    val state = rememberWebViewState(url)
    // Must be the WebView's own cookie manager: on desktop the native cookie store is
    // attached to it inside the WebView composable, and a freshly constructed
    // WebViewCookieManager() is unattached — its getCookies() silently returns an empty
    // list, so the clearance could never be captured. (Android/iOS use a global store, so
    // this is equivalent there.)
    val cookieManager = state.cookieManager
    // Deliberately do NOT pin a desktop Chrome User-Agent here. The desktop engine is
    // WebKit2GTK (Android WebView / WKWebView on mobile), and Cloudflare Turnstile
    // fingerprints the engine — a Chrome UA on WebKit leaves the challenge spinning and
    // failing. The engine default is used, and the UA it actually reports is stored with
    // the cookie below.

    // Poll for the clearance cookie; cf_clearance is HttpOnly, so a JS read can't see it.
    LaunchedEffect(host) {
        repeat(90) {
            delay(1_000)
            val cookies = runCatching { cookieManager.getCookies(url) }.getOrDefault(emptyList())
            if (cookies.any { it.name == "cf_clearance" }) {
                val header = cookies.joinToString("; ") { "${it.name}=${it.value}" }
                // Cloudflare binds `cf_clearance` to the UA that passed the challenge, so
                // store the UA the engine actually reported (not a spoofed one).
                val userAgent = state.webView?.userAgentOrNull()
                    ?: platformUserAgent()
                    ?: DEFAULT_USER_AGENT
                Logger.i("Cloudflare solve: captured clearance for $host (ua=$userAgent)")
                store.completeSolve(host, CloudflareClearance(header, userAgent))
                onFinished()
                return@LaunchedEffect
            }
        }
        store.completeSolve(host, null)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(CornerRadius.md),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "${s.label} · $host",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
                Text(
                    text = s.solving,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.md),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                ) {
                    WebViewPlatform {
                        WebView(state = state, modifier = Modifier.fillMaxSize())
                    }
                }
                Row(modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        scope.launch { store.completeSolve(host, null) }
                        onFinished()
                    }) {
                        Text(s.cancel)
                    }
                }
            }
        }
    }
}

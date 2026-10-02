package fr.vinarnt.animu.finder.compose.navigation.screen.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewState
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.repository.provider.DEFAULT_USER_AGENT
import fr.vinarnt.animu.finder.compose.service.CloudflareClearanceStore
import fr.vinarnt.animu.finder.compose.ui.component.base.layout.MainLayout
import fr.vinarnt.animu.finder.compose.ui.component.navigation.bar.NavigationBar
import fr.vinarnt.animu.finder.compose.ui.component.setting.WebViewPlatform
import fr.vinarnt.animu.finder.compose.ui.component.setting.platformUserAgent
import fr.vinarnt.animu.finder.compose.ui.component.setting.supportsInAppCookieCapture
import fr.vinarnt.animu.finder.compose.ui.component.setting.userAgentOrNull
import fr.vinarnt.animu.finder.compose.ui.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Reusable Cloudflare clearance capture for any provider host.
 *
 * Loads the site in an in-app WebView so the user can clear the challenge. "Capture" reads the
 * (HttpOnly) `cf_clearance` cookie and the WebView's User-Agent and persists both, since
 * Cloudflare binds the cookie to that User-Agent. A manual paste fallback is always available,
 * and is the only path on wasm.
 */
class CloudflareClearanceScreen(
    private val label: String,
    private val host: String,
) : Screen {

    override val key: ScreenKey = "cloudflare-$host"

    @Composable
    override fun Content() {
        val s = strings.settings.cloudflare
        val store: CloudflareClearanceStore = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val uriHandler = LocalUriHandler.current
        val url = "https://$host/"

        val clearancesFlow = remember { store.clearances() }
        val clearances by clearancesFlow.collectAsStateWithLifecycle(emptyMap())
        val current = clearances[host]

        // Pin no User-Agent: the engine's own is used so Cloudflare Turnstile's fingerprint
        // check is consistent (a Chrome UA on a WebKit engine fails). The UA the engine
        // reports is read back and used as the paste hint / capture fallback.
        var detectedUserAgent by remember { mutableStateOf<String?>(null) }
        val suggestedUserAgent = detectedUserAgent ?: platformUserAgent() ?: DEFAULT_USER_AGENT
        var cookieInput by remember(current) { mutableStateOf(current?.cookie.orEmpty()) }
        var userAgentInput by remember(current) { mutableStateOf(current?.userAgent.orEmpty()) }
        var message by remember { mutableStateOf<String?>(null) }

        val state = rememberWebViewState(url)
        val webNavigator = rememberWebViewNavigator()
        // Must be the WebView's own manager: on desktop the native cookie store is attached
        // to it by the WebView composable, and a fresh WebViewCookieManager() is unattached
        // (getCookies() returns an empty list, so Capture always failed on desktop).
        val cookieManager = state.cookieManager

        LaunchedEffect(state.webView) {
            state.webView?.let { detectedUserAgent = it.userAgentOrNull() }
        }

        MainLayout(
            topBar = {
                NavigationBar(
                    title = label,
                    actions = {
                        if (supportsInAppCookieCapture) {
                            Button(onClick = {
                                scope.launch {
                                    val cookies = runCatching { cookieManager.getCookies(url) }
                                        .getOrDefault(emptyList())
                                    val clearance = cookies.firstOrNull { it.name == "cf_clearance" }
                                    if (clearance == null) {
                                        Logger.w("Cloudflare capture: no cf_clearance for $host (cookies=${cookies.map { it.name }})")
                                        message = s.captureFailed
                                        return@launch
                                    }
                                    val header = cookies.joinToString("; ") { "${it.name}=${it.value}" }
                                    // cf_clearance is bound to the UA that solved it.
                                    val userAgent = state.webView?.userAgentOrNull() ?: suggestedUserAgent
                                    store.save(host, header, userAgent)
                                    Logger.i("Cloudflare capture: saved clearance for $host (cookies=${cookies.map { it.name }})")
                                    navigator.pop()
                                }
                            }) {
                                Text(s.captureButton)
                            }
                        }
                    },
                )
            },
            scrollable = false,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = s.captureHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = Spacing.md),
                    )
                }

                if (supportsInAppCookieCapture) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        WebViewPlatform {
                            WebView(
                                state = state,
                                navigator = webNavigator,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                } else {
                    Text(
                        text = s.captureUnsupported,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.md),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    CookieField(
                        value = cookieInput,
                        onValueChange = { cookieInput = it },
                        label = s.cookieLabel,
                        placeholder = s.cookieHint,
                    )
                    CookieField(
                        value = userAgentInput,
                        onValueChange = { userAgentInput = it },
                        label = s.userAgentLabel,
                        placeholder = suggestedUserAgent ?: s.userAgentHint,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            scope.launch {
                                store.save(
                                    host = host,
                                    cookie = cookieInput,
                                    userAgent = userAgentInput.ifBlank { suggestedUserAgent },
                                )
                                message = s.captureSuccess
                            }
                        }) {
                            Text(s.save)
                        }
                        TextButton(onClick = {
                            cookieInput = ""
                            userAgentInput = ""
                            scope.launch { store.remove(host) }
                        }) {
                            Text(s.clear)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { uriHandler.openUri(url) }) {
                            Text(s.openSite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CookieField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.labelSmall) },
        textStyle = MaterialTheme.typography.bodySmall,
        maxLines = 3,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

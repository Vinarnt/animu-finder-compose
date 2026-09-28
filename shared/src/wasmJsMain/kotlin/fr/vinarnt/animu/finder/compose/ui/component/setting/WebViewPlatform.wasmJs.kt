package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable
import kotlinx.browser.window

/** The browser's own cookie jar is off-limits to JS (SOP + HttpOnly), so no auto-capture. */
actual val supportsInAppCookieCapture: Boolean = false

actual val isInAppWebViewReady: Boolean = false

@Composable
actual fun WebViewPlatform(content: @Composable () -> Unit) = content()

/** The wasm host is the browser itself, so it can report its exact User-Agent. */
actual fun platformUserAgent(): String? = window.navigator.userAgent

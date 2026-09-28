package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable

actual val supportsInAppCookieCapture: Boolean = true

/** The system WebView is always available. */
actual val isInAppWebViewReady: Boolean = true

@Composable
actual fun WebViewPlatform(content: @Composable () -> Unit) = content()

actual fun platformUserAgent(): String? = null

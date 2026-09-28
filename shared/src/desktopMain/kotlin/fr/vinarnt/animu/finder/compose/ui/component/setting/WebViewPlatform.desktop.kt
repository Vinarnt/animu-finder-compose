package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable

actual val supportsInAppCookieCapture: Boolean = true

/** The desktop WebView uses the system engine (WebKit2GTK / WKWebView / WebView2): always ready. */
actual val isInAppWebViewReady: Boolean = true

actual fun platformUserAgent(): String? = null

@Composable
actual fun WebViewPlatform(content: @Composable () -> Unit) = content()

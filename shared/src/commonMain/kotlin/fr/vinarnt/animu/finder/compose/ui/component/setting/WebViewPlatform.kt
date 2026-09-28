package fr.vinarnt.animu.finder.compose.ui.component.setting

import androidx.compose.runtime.Composable

/**
 * Whether the platform can read the WebView's cookies, i.e. auto-capture the
 * Cloudflare `cf_clearance` cookie that Nakanime issues.
 *
 * False on wasm: the same-origin policy hides another origin's cookies from
 * JavaScript (and `cf_clearance` is HttpOnly anyway), so the web build falls back
 * to the manual paste flow.
 */
expect val supportsInAppCookieCapture: Boolean

/**
 * Whether the in-app WebView engine is ready to use right now. All native targets use the
 * platform's own web engine (Android WebView, WKWebView, WebKit2GTK/WebView2 on desktop),
 * so this is always true there; false only on wasm, where there is no in-app WebView.
 */
expect val isInAppWebViewReady: Boolean

/**
 * Lets a platform wrap its WebView content. All targets are pass-throughs now that the
 * desktop backend is the system engine (no bundled browser to initialize).
 */
@Composable
expect fun WebViewPlatform(content: @Composable () -> Unit)

/**
 * The platform's browser User-Agent when it can be read, else `null`. Used on wasm
 * so a manually pasted `cf_clearance` cookie is replayed with the same UA that
 * solved the challenge (Cloudflare binds the two).
 */
expect fun platformUserAgent(): String?

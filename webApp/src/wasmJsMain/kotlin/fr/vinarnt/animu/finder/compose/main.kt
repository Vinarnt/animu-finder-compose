package fr.vinarnt.animu.finder.compose

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * WasmJS entry point.
 *
 * FROZEN: dev/preview only, not a deployment target (see AGENTS.md → "Target status").
 * Kept so the shared Compose UI can be exercised in a browser; the shipping targets
 * are desktop, Android and iOS. Don't add provider/platform work that only serves this.
 */
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

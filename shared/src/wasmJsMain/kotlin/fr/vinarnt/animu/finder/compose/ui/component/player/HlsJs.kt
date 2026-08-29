@file:OptIn(ExperimentalWasmJsInterop::class)
@file:JsModule("hls.js")

package fr.vinarnt.animu.finder.compose.ui.component.player

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import org.w3c.dom.HTMLVideoElement

/**
 * Minimal external bindings for the hls.js default player. The web player uses it
 * to play cross-origin HLS streams that desktop browsers cannot decode natively.
 */
external class Hls(config: JsAny? = definedExternally) {
    fun attachMedia(element: HTMLVideoElement)
    fun loadSource(url: String)
    fun destroy()

    fun on(event: String, listener: (JsAny?, JsAny?) -> Unit)

    companion object {
        fun isSupported(): Boolean
    }
}
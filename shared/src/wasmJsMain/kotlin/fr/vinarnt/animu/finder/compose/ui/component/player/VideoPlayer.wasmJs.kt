@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    ExperimentalWasmJsInterop::class,
)

package fr.vinarnt.animu.finder.compose.ui.component.player

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.viewinterop.HtmlElementView
import fr.vinarnt.animu.finder.compose.i18n.strings
import fr.vinarnt.animu.finder.compose.model.StreamSource
import fr.vinarnt.animu.finder.compose.model.SubtitlePosition
import fr.vinarnt.animu.finder.compose.repository.provider.proxyMediaUrl
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event

private const val SeekStepMillis = 10_000L
private val WebSpeedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
private var PlayerIdCounter = 0

private const val PlaySvg = "<svg viewBox='0 0 24 24' width='24' height='24' fill='#fff'><path d='M8 5v14l11-7z'/></svg>"
private const val PauseSvg = "<svg viewBox='0 0 24 24' width='24' height='24' fill='#fff'><path d='M6 19h4V5H6v14zm8-14v14h4V5h-4z'/></svg>"
private const val FullscreenEnterSvg = "<svg viewBox='0 0 24 24' width='24' height='24' fill='#fff'><path d='M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z'/></svg>"
private const val FullscreenExitSvg = "<svg viewBox='0 0 24 24' width='24' height='24' fill='#fff'><path d='M5 16h3v3h2v-5H5v2zm3-8H5v2h5V5H8v3zm6 11h2v-3h3v-2h-5v5zm2-11V5h-2v5h5V8h-3z'/></svg>"

/** Controls/loading/error rendered by the web player, as DOM nodes above the video. */
private class DomPlayerControls(
    val tapLayer: HTMLElement,
    val subtitle: HTMLElement,
    val controls: HTMLElement,
    val playBtn: HTMLElement,
    val slider: HTMLInputElement,
    val timeText: HTMLElement,
    val speedBtn: HTMLElement,
    val fullscreenBtn: HTMLElement,
    val loading: HTMLElement,
    val error: HTMLElement,
    val retryBtn: HTMLElement,
)

/** Distance in CSS pixels a pointer must move before the subtitle drag begins. */
private const val SubtitleDragSlopPx = 6.0

/**
 * Live state for the subtitle drag gesture. All fields are read/written from DOM
 * event handlers, so a plain mutable holder (not Compose state) is used.
 */
private class SubtitleDragState {
    var pointerId = -1
    var startX = 0.0
    var startY = 0.0
    var startPosX = 0f
    var startPosY = 0f
    var dragged = false
    /** Set when a drag (not a tap) just ended, to suppress the trailing click. */
    var wasDrag = false
}

/**
 * Web (wasmJs) streaming video player.
 *
 * Desktop browsers cannot decode HLS natively and block cross-origin media without
 * CORS headers, so this drives a native [HTMLVideoElement] (via [HtmlElementView])
 * and:
 * - routes the media URL through a CORS proxy ([proxyMediaUrl]),
 * - plays `.m3u8` sources through hls.js when MSE is available, falling back to the
 *   native element (e.g. Safari's native HLS) otherwise.
 *
 * Overlay input/visibility: the Skia/canvas web renderer inserts the
 * `HtmlElementView` content in the light DOM, on top of the canvas. Compose can never
 * paint above it, so the control bar, loading spinner and error state are built as real
 * DOM nodes inside the video container (the same tree as the video), layered above the
 * video element. The container is `pointer-events: none` and only the interactive
 * overlay nodes are targetable, so taps/hover land where expected.
 */
@Composable
actual fun StreamingVideoPlayer(
    source: StreamSource,
    modifier: Modifier,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
) {
    key(source.url) {
        val container = remember {
            (document.createElement("div") as HTMLDivElement).also {
                it.id = "afp-player-" + (++PlayerIdCounter)
            }
        }
        val playerId = container.id
        val videoElement = remember { document.createElement("video") as HTMLVideoElement }
        val proxiedUrl = remember(source.url) { proxyMediaUrl(source.url) }

        var isPlaying by remember { mutableStateOf(false) }
        var positionMs by remember { mutableStateOf(0L) }
        var durationMs by remember { mutableStateOf(0L) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf(false) }
        var speed by remember { mutableStateOf(1f) }
        var dragging by remember { mutableStateOf(false) }

        var retryAttempt by remember { mutableStateOf(0) }
        val hlsSupported = remember { Hls.isSupported() }
        var useHlsJs by remember {
            mutableStateOf(hlsSupported && (source.isM3U8 || looksLikeHlsUrl(source.url)))
        }
        var triedHls by remember { mutableStateOf(useHlsJs) }
        var triedNative by remember { mutableStateOf(!useHlsJs) }

        val playerErrorText = strings.player.error
        val playerRetryText = strings.player.retry
        val dom = remember { buildDomNodes(playerErrorText, playerRetryText) }

        // Soft subtitles are external WebVTT files loaded by the shared subtitle
        // state and rendered here as a DOM node above the video (mirroring the
        // desktop SubtitleOverlay). Hard subs are burned into the video and need
        // no overlay.
        val subtitleState = rememberSoftSubtitleState(source.subtitles, positionMs)
        val subtitleDrag = remember { SubtitleDragState() }

        DisposableEffect(videoElement, proxiedUrl) {
            videoElement.controls = false
            videoElement.playsInline = true
            videoElement.preload = "metadata"

            val removers = mutableListOf<() -> Unit>()
            fun on(type: String, handler: () -> Unit) {
                val listener: (Event) -> Unit = { handler() }
                videoElement.addEventListener(type, listener)
                removers += { videoElement.removeEventListener(type, listener) }
            }

            on("play") { isPlaying = true }
            on("playing") { isPlaying = true }
            on("pause") { isPlaying = false }
            on("timeupdate") { positionMs = (videoElement.currentTime * 1000).roundToLong() }
            on("loadedmetadata") {
                durationMs = if (videoElement.duration.isFinite()) {
                    (videoElement.duration * 1000).roundToLong()
                } else 0L
            }
            on("durationchange") {
                durationMs = if (videoElement.duration.isFinite()) {
                    (videoElement.duration * 1000).roundToLong()
                } else 0L
            }
            on("waiting") { loading = true }
            on("canplay") { loading = false }

            onDispose {
                removers.forEach { it() }
            }
        }

        // Load the media. HLS goes through hls.js (so segments are fetched through the
        // same proxy), everything else is handed to the native element directly. If the
        // chosen path turns out to be wrong (e.g. a `.m3u8` label that is actually an
        // MP4, or an HLS manifest served without a `.m3u8` extension), it falls back to
        // the other path once.
        DisposableEffect(videoElement, proxiedUrl, source.headers, useHlsJs, retryAttempt) {
            error = false
            loading = true
            videoElement.removeAttribute("src")
            videoElement.load()

            if (useHlsJs) {
                val headersObj = emptyJsObject()
                source.headers.forEach { (k, v) -> setJsProperty(headersObj, k, v) }
                val instance = Hls(createHlsConfig(headersObj))
                var destroyed = false
                instance.on("hlsManifestParsed") { _, _ -> loading = false }
                instance.on("hlsError") { _, data ->
                    if (isFatalHlsError(data)) {
                        if (!triedNative) {
                            destroyed = true
                            instance.destroy()
                            useHlsJs = false
                            triedNative = true
                        } else {
                            error = true
                            loading = false
                        }
                    }
                }
                instance.attachMedia(videoElement)
                instance.loadSource(proxiedUrl)
                onDispose {
                    if (!destroyed) instance.destroy()
                    videoElement.removeAttribute("src")
                    videoElement.load()
                }
            } else {
                videoElement.src = proxiedUrl
                videoElement.load()
                val errorListener: (Event) -> Unit = {
                    if (!triedHls && looksLikeHlsUrl(source.url)) {
                        useHlsJs = true
                        triedHls = true
                    } else {
                        error = true
                        loading = false
                    }
                }
                videoElement.addEventListener("error", errorListener)
                onDispose {
                    videoElement.removeEventListener("error", errorListener)
                    videoElement.removeAttribute("src")
                    videoElement.load()
                }
            }
        }

        // Assemble the DOM player: video element + overlay nodes, all inside the
        // container that HtmlElementView attaches to the Compose scene. The overlay
        // nodes are real DOM layered above the video so they are visible and clickable.
        DisposableEffect(container, videoElement, dom, playerId) {
            val styleEl = document.createElement("style")
            styleEl.textContent = playerCss(playerId)
            container.appendChild(styleEl)
            container.appendChild(videoElement)
            container.appendChild(dom.tapLayer)
            container.appendChild(dom.subtitle)
            container.appendChild(dom.loading)
            container.appendChild(dom.error)
            container.appendChild(dom.controls)

            val removers = mutableListOf<() -> Unit>()
            fun on(el: HTMLElement, type: String, handler: () -> Unit) {
                val listener: (Event) -> Unit = { handler() }
                el.addEventListener(type, listener)
                removers += { el.removeEventListener(type, listener) }
            }
            fun onEvent(el: HTMLElement, type: String, handler: (Event) -> Unit) {
                val listener: (Event) -> Unit = { handler(it) }
                el.addEventListener(type, listener)
                removers += { el.removeEventListener(type, listener) }
            }

            on(dom.playBtn, "click") { togglePlay(videoElement, isPlaying) }
            on(dom.tapLayer, "click") { togglePlay(videoElement, isPlaying) }
            on(dom.tapLayer, "dblclick") { toggleNativeFullscreen(container) }

            // The subtitle overlay is draggable like the desktop one. It owns its
            // pointer events: a clean tap toggles play (mirroring the tap layer) and
            // a real drag repositions it, persisting the new spot on release. The
            // drag is tracked from the rendered DOM position (left/top percentages
            // set by the shared subtitle state), not from a captured Compose value.
            onEvent(dom.subtitle, "pointerdown") { e ->
                subtitleDrag.pointerId = pointerIdOf(e)
                subtitleDrag.startX = clientXOf(e)
                subtitleDrag.startY = clientYOf(e)
                subtitleDrag.dragged = false
                val subRect = dom.subtitle.getBoundingClientRect()
                val containerRect = container.getBoundingClientRect()
                subtitleDrag.startPosX =
                    ((subRect.left + subRect.width / 2 - containerRect.left) / container.clientWidth.toDouble()).toFloat()
                subtitleDrag.startPosY =
                    ((subRect.top + subRect.height / 2 - containerRect.top) / container.clientHeight.toDouble()).toFloat()
                capturePointer(e)
                e.preventDefault()
            }
            onEvent(dom.subtitle, "pointermove") { e ->
                if (subtitleDrag.pointerId != pointerIdOf(e)) return@onEvent
                val dx = clientXOf(e) - subtitleDrag.startX
                val dy = clientYOf(e) - subtitleDrag.startY
                if (!subtitleDrag.dragged) {
                    if (abs(dx) < SubtitleDragSlopPx && abs(dy) < SubtitleDragSlopPx) return@onEvent
                    subtitleDrag.dragged = true
                    dom.subtitle.style.cursor = "grabbing"
                }
                val playerW = container.clientWidth.toFloat().coerceAtLeast(1f)
                val playerH = container.clientHeight.toFloat().coerceAtLeast(1f)
                val newX = (subtitleDrag.startPosX + dx.toFloat() / playerW).coerceIn(0f, 1f)
                val newY = (subtitleDrag.startPosY + dy.toFloat() / playerH).coerceIn(0f, 1f)
                subtitleState.movePosition(SubtitlePosition(newX, newY))
                e.preventDefault()
            }
            onEvent(dom.subtitle, "pointerup") { e ->
                if (subtitleDrag.pointerId != pointerIdOf(e)) return@onEvent
                releasePointerCapture(e)
                subtitleDrag.pointerId = -1
                if (subtitleDrag.dragged) {
                    subtitleDrag.wasDrag = true
                    subtitleState.commitPosition()
                }
                subtitleDrag.dragged = false
                dom.subtitle.style.cursor = "grab"
            }
            onEvent(dom.subtitle, "pointercancel") {
                subtitleDrag.pointerId = -1
                subtitleDrag.dragged = false
            }
            onEvent(dom.subtitle, "lostpointercapture") {
                subtitleDrag.pointerId = -1
                subtitleDrag.dragged = false
            }
            onEvent(dom.subtitle, "click") { e ->
                if (subtitleDrag.wasDrag) {
                    subtitleDrag.wasDrag = false
                    e.preventDefault()
                    return@onEvent
                }
                togglePlay(videoElement, isPlaying)
            }
            onEvent(dom.subtitle, "dblclick") { toggleNativeFullscreen(container) }
            on(dom.speedBtn, "click") { speed = nextSpeed(speed) }
            on(dom.fullscreenBtn, "click") { toggleNativeFullscreen(container) }
            on(dom.retryBtn, "click") { retryAttempt++ }
            on(dom.slider, "input") {
                dragging = true
                dom.timeText.textContent = "${formatTime(dom.slider.value.toLong())} / ${formatTime(durationMs)}"
            }
            on(dom.slider, "change") {
                dragging = false
                seek(videoElement, dom.slider.value.toLong())
            }

            // Native fullscreen targets the container element so the browser sizes it
            // to the actual screen (independent of the Compose canvas, which stays at
            // the window size). Sync the fullscreen button icon with the real state.
            val fullscreenChange: (Event) -> Unit = {
                val active = document.fullscreenElement == container
                dom.fullscreenBtn.innerHTML = if (active) FullscreenExitSvg else FullscreenEnterSvg
            }
            document.addEventListener("fullscreenchange", fullscreenChange)
            removers += { document.removeEventListener("fullscreenchange", fullscreenChange) }

            onDispose {
                removers.forEach { it() }
                container.textContent = ""
            }
        }

        // Mirror Compose state into the DOM overlay nodes.
        LaunchedEffect(dom, isPlaying, loading, error) {
            dom.playBtn.innerHTML = if (isPlaying) PauseSvg else PlaySvg
            if (!isPlaying || loading || error) {
                container.classList.add("afp-pinned")
            } else {
                container.classList.remove("afp-pinned")
            }
        }
        LaunchedEffect(dom, loading) {
            dom.loading.style.display = if (loading) "flex" else "none"
        }
        LaunchedEffect(dom.subtitle, subtitleState.activeCue) {
            if (subtitleState.activeCue != null) {
                dom.subtitle.textContent = subtitleState.activeCue.text
                dom.subtitle.style.display = "block"
            } else {
                dom.subtitle.textContent = ""
                dom.subtitle.style.display = "none"
            }
        }
        LaunchedEffect(dom.subtitle, subtitleState.position) {
            dom.subtitle.style.left = "${subtitleState.position.x * 100}%"
            dom.subtitle.style.top = "${subtitleState.position.y * 100}%"
        }
        LaunchedEffect(dom, error) {
            dom.error.style.display = if (error) "flex" else "none"
        }
        LaunchedEffect(dom, positionMs, durationMs) {
            val maxMs = durationMs.coerceAtLeast(1L)
            dom.slider.max = maxMs.toString()
            if (!dragging) {
                dom.slider.value = positionMs.coerceIn(0L, maxMs).toString()
            }
            dom.timeText.textContent = "${formatTime(positionMs)} / ${formatTime(durationMs)}"
        }
        LaunchedEffect(dom, speed) {
            dom.speedBtn.textContent = formatSpeed(speed) + "x"
            videoElement.playbackRate = speed.toDouble()
        }

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Box(
            modifier = (if (isFullscreen) modifier.fillMaxSize() else modifier.fillMaxWidth().aspectRatio(16f / 9f))
                .background(Color.Black)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Spacebar -> togglePlay(videoElement, isPlaying)
                        Key.DirectionLeft -> seek(videoElement, positionMs - SeekStepMillis)
                        Key.DirectionRight -> seek(videoElement, positionMs + SeekStepMillis)
                        Key.F -> toggleNativeFullscreen(container)
                        else -> return@onPreviewKeyEvent false
                    }
                    true
                },
        ) {
            HtmlElementView(
                factory = { container },
                modifier = Modifier.fillMaxSize(),
                update = { element ->
                    // The canvas renderer inserts this element (and its wrapper) in the
                    // light DOM on top of the Compose scene. Pointer events must reach the
                    // DOM overlay nodes and nothing else: the container ignores input
                    // while the interactive overlay children accept it (see playerCss).
                    element.style.setProperty("pointer-events", "none")
                    (element.parentElement as? HTMLElement)?.style?.setProperty("pointer-events", "none")
                },
            )
        }
    }
}

private fun togglePlay(videoElement: HTMLVideoElement, isPlaying: Boolean) {
    if (isPlaying) {
        videoElement.pause()
    } else {
        videoElement.play()
    }
}

private fun seek(videoElement: HTMLVideoElement, targetMs: Long) {
    if (!videoElement.duration.isFinite()) return
    val target = targetMs.coerceIn(0L, (videoElement.duration * 1000).roundToLong()) / 1000f
    videoElement.currentTime = target.toDouble()
}

/**
 * Toggles the browser's native fullscreen on the player container. Fullscreening the
 * container element (rather than the whole document) makes the browser resize it to
 * the actual screen, so the video fills the display even though the Compose canvas
 * stays sized to the window.
 */
private fun toggleNativeFullscreen(container: HTMLDivElement) {
    if (document.fullscreenElement == null) {
        requestFullscreenElement(container)
    } else {
        exitFullscreen(document)
    }
}

@JsFun("(el) => { const p = el.requestFullscreen(); if (p && typeof p.catch === 'function') { p.catch(() => {}); } }")
private external fun requestFullscreenElement(el: Element)

@JsFun("(doc) => { const p = doc.exitFullscreen(); if (p && typeof p.catch === 'function') { p.catch(() => {}); } }")
private external fun exitFullscreen(doc: Document)

private fun nextSpeed(current: Float): Float {
    val index = WebSpeedOptions.indexOf(current).coerceAtLeast(0)
    return WebSpeedOptions[(index + 1) % WebSpeedOptions.size]
}

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

private fun buildDomNodes(errorText: String, retryText: String): DomPlayerControls {
    fun el(tag: String): HTMLElement = document.createElement(tag) as HTMLElement

    val tapLayer = el("div")
    tapLayer.className = "afp-tap"

    val subtitle = el("div")
    subtitle.className = "afp-subtitle"

    val controls = el("div")
    controls.className = "afp-controls"

    val slider = document.createElement("input") as HTMLInputElement
    slider.type = "range"
    slider.className = "afp-slider"
    slider.min = "0"
    slider.max = "1"
    slider.value = "0"

    val buttons = el("div")
    buttons.className = "afp-buttons"

    val playBtn = el("button")
    playBtn.className = "afp-btn"
    playBtn.innerHTML = PlaySvg

    val timeText = el("span")
    timeText.className = "afp-time"
    timeText.textContent = "0:00 / 0:00"

    val spacer = el("span")
    spacer.className = "afp-spacer"

    val speedBtn = el("button")
    speedBtn.className = "afp-speed"
    speedBtn.textContent = "1x"

    val fullscreenBtn = el("button")
    fullscreenBtn.className = "afp-btn"
    fullscreenBtn.innerHTML = FullscreenEnterSvg

    buttons.appendChild(playBtn)
    buttons.appendChild(timeText)
    buttons.appendChild(spacer)
    buttons.appendChild(speedBtn)
    buttons.appendChild(fullscreenBtn)

    controls.appendChild(slider)
    controls.appendChild(buttons)

    val loading = el("div")
    loading.className = "afp-loading"
    val spinner = el("div")
    spinner.className = "afp-spinner"
    loading.appendChild(spinner)

    val error = el("div")
    error.className = "afp-error"
    val errorTextEl = el("span")
    errorTextEl.className = "afp-error-text"
    errorTextEl.textContent = errorText
    val retryBtn = el("button")
    retryBtn.className = "afp-retry"
    retryBtn.textContent = retryText
    error.appendChild(errorTextEl)
    error.appendChild(retryBtn)

    return DomPlayerControls(
        tapLayer = tapLayer,
        subtitle = subtitle,
        controls = controls,
        playBtn = playBtn,
        slider = slider,
        timeText = timeText,
        speedBtn = speedBtn,
        fullscreenBtn = fullscreenBtn,
        loading = loading,
        error = error,
        retryBtn = retryBtn,
    )
}

private fun playerCss(id: String): String = """
    #$id { position: relative; width: 100%; height: 100%; background: #000; overflow: hidden; }
    #$id video { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: contain; pointer-events: none; }
    #$id .afp-tap { position: absolute; inset: 0; z-index: 2; cursor: pointer; pointer-events: auto; }
    #$id .afp-subtitle { position: absolute; left: 50%; top: 75%; transform: translate(-50%, -50%); z-index: 2; max-width: 90%; color: #fff; font: 16px/1.5 system-ui, -apple-system, sans-serif; font-weight: 600; text-align: center; text-shadow: 0 2px 8px rgba(0,0,0,.95); white-space: pre-line; pointer-events: auto; cursor: grab; touch-action: none; user-select: none; display: none; }
    #$id .afp-controls { position: absolute; left: 0; right: 0; bottom: 0; z-index: 3; display: flex; flex-direction: column; gap: 4px; padding: 10px 14px; box-sizing: border-box; background: linear-gradient(to top, rgba(0,0,0,.85), rgba(0,0,0,0)); opacity: 0; pointer-events: none; transition: opacity .2s ease; cursor: default; }
    #$id:hover .afp-controls, #$id.afp-pinned .afp-controls { opacity: 1; pointer-events: auto; }
    #$id .afp-slider { width: 100%; margin: 0; accent-color: #fff; cursor: pointer; height: 4px; }
    #$id .afp-buttons { display: flex; align-items: center; gap: 12px; }
    #$id .afp-btn { background: none; border: none; padding: 4px; cursor: pointer; display: inline-flex; line-height: 0; }
    #$id .afp-btn svg { display: block; }
    #$id .afp-time { color: #fff; font: 13px system-ui, -apple-system, sans-serif; font-variant-numeric: tabular-nums; }
    #$id .afp-speed { color: #fff; font: 13px system-ui, -apple-system, sans-serif; background: none; border: none; cursor: pointer; padding: 4px 6px; }
    #$id .afp-spacer { flex: 1; }
    #$id .afp-loading { position: absolute; inset: 0; z-index: 3; display: none; align-items: center; justify-content: center; }
    #$id .afp-spinner { width: 46px; height: 46px; border: 4px solid rgba(255,255,255,.25); border-top-color: #fff; border-radius: 50%; animation: afp-spin .8s linear infinite; }
    #$id .afp-error { position: absolute; inset: 0; z-index: 3; display: none; flex-direction: column; align-items: center; justify-content: center; gap: 10px; padding: 20px; box-sizing: border-box; }
    #$id .afp-error-text { color: #fff; font: 14px system-ui, -apple-system, sans-serif; text-align: center; }
    #$id .afp-retry { background: rgba(255,255,255,.18); color: #fff; border: none; border-radius: 18px; padding: 8px 22px; font: 14px system-ui, -apple-system, sans-serif; cursor: pointer; }
    @keyframes afp-spin { to { transform: rotate(360deg); } }
""".trimIndent()

@JsFun("() => ({})")
private external fun emptyJsObject(): JsAny

@JsFun("(obj, key, value) => { obj[key] = value; }")
private external fun setJsProperty(obj: JsAny?, key: String, value: String)

@JsFun("(e) => e.pointerId")
private external fun pointerIdOf(e: Event): Int

@JsFun("(e) => e.clientX")
private external fun clientXOf(e: Event): Double

@JsFun("(e) => e.clientY")
private external fun clientYOf(e: Event): Double

@JsFun("(e) => { try { e.setPointerCapture(e.pointerId); } catch (err) {} }")
private external fun capturePointer(e: Event)

@JsFun("(e) => { if (e.hasPointerCapture && e.hasPointerCapture(e.pointerId)) e.releasePointerCapture(e.pointerId); }")
private external fun releasePointerCapture(e: Event)

@JsFun(
    "(headers) => ({ enableWorker: false, " +
        "manifestLoadingMaxRetry: 4, manifestLoadingRetryDelay: 500, manifestLoadingMaxRetryTimeout: 15000, " +
        "levelLoadingMaxRetry: 4, levelLoadingRetryDelay: 300, levelLoadingMaxRetryTimeout: 15000, " +
        "fragLoadingMaxRetry: 6, fragLoadingRetryDelay: 500, fragLoadingMaxRetryTimeout: 30000, " +
        "fragLoadingTimeOut: 30000, maxBufferLength: 60, " +
        "xhrSetup: (xhr, url) => { " +
        "const keys = Object.keys(headers); for (const k of keys) { " +
        "try { xhr.setRequestHeader(k, headers[k]); } catch (e) {} } } })",
)
private external fun createHlsConfig(headers: JsAny?): JsAny

@JsFun("(data) => data != null && data.fatal === true")
private external fun isFatalHlsError(data: JsAny?): Boolean

/**
 * Whether [url] is likely an HLS playlist, even when the extension is not `.m3u8`
 * (e.g. some providers serve HLS manifests as `master.txt` behind `/hls/` paths).
 */
private fun looksLikeHlsUrl(url: String): Boolean {
    val lower = url.lowercase()
    return lower.contains(".m3u8") ||
        lower.contains("/m3u8") ||
        (lower.contains("/hls") && (lower.contains("master") || lower.contains("playlist")))
}
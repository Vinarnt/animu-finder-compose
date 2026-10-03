# AGENTS.md

This file provides guidance to AI coding agents (OpenCode, Claude Code, Codex, Cursor, etc.) when working with code in this repository.

Compose/CMP work: load the compose skill first; it picks the path and the files to read.

## Build & Run Commands

```shell
# Desktop
./gradlew :desktopApp:run

# Web (WASM) — dev/preview only; wasm is frozen, see "Target status"
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Compile check (fast, no run)
./gradlew :shared:compileKotlinDesktop
./gradlew :shared:compileKotlinWasmJs

# Android: use IntelliJ IDEA or Android Studio, run the androidApp MainActivity configuration
```

There are no tests in this project.

## Architecture Overview

**Stack:** Kotlin Multiplatform (Android, iOS, Desktop JVM, WasmJS) + Compose Multiplatform + Material3, using the new default KMP project structure. Shared code lives in the `shared` KMP library; each platform's runnable app is its own module (`androidApp`, `desktopApp`, `webApp`).

**Modules:**
- `shared` — KMP library with all shared code and platform expect/actuals (`commonMain`, `androidMain`, `desktopMain`, `iosMain`, `wasmJsMain`, plus an intermediate `nonJsMain` source set). Hosts `App.kt`, all screens/viewmodels/repositories, and the iOS `MainViewController` entry point (the iOS app isn't a Gradle module). The Android target uses the `com.android.kotlin.multiplatform.library` (Android-KMP) plugin, configured via `kotlin.androidLibrary {}` — it is a library, not an application. The desktop and web entry points (`main.kt`) live in `desktopApp`/`webApp`, not here.
- `androidApp` — pure Android application module (`com.android.application`, built-in Kotlin). Hosts `MainActivity`, the `AndroidManifest.xml`, and Android `res/`; depends on `shared`. Configured with the AGP 9 `kotlin.target {}` DSL.
- `desktopApp` — pure JVM application module (`org.jetbrains.kotlin.jvm` + Compose). Hosts the desktop `main.kt` entry point and the `compose.desktop {}` packaging config; depends on `shared`. The window is a **Nucleus Tao** `DecoratedWindow` (`nucleusApplication(backend = NucleusBackend.Tao)`), required by the native WebView; desktop WebView uses the system engine via Nucleus (`composewebview`) — **WebKit2GTK** on Linux (runtime dep), WKWebView on macOS, WebView2 on Windows. No bundled browser, no JBR/JCEF requirement.
- `webApp` — pure WasmJS application module (`kotlinMultiplatform` + Compose). Hosts the web `main.kt` entry point, webpack config, and `index.html`; depends on `shared`. **Frozen: dev/preview only, not a deployment target** (see "Target status").

**Layers:**
1. `navigation/screen/` — Voyager `Screen` implementations, own their `@Composable Content()`. Screens pull ViewModels via `koinViewModel()`.
2. `viewmodel/` — `androidx.lifecycle.ViewModel` subclasses, expose `StateFlow`s.
3. `repository/` — data access layer:
   - `AnimeRepository.kt` — wraps `JikanClient` (jikan4k library, Tenrai API); returns `ResponseState` sealed type (`Loading / Success / Error / None`).
   - `provider/` — video streaming. `StreamingProvider` interface → `BaseProvider` (shared title/episode matching via Levenshtein similarity) → per-provider implementations (`VoirAnimeProvider`, `AnimeSamaProvider`, `NakanimeProvider`, `AnimePaheProvider`, `AnimeYaProvider`). `StreamRepository` fans out provider extraction concurrently (`callbackFlow`). `ProviderHttp`/`ProviderHttpClient` use expect/actual per platform.
     - `NakanimeProvider` (nakanime.tv) talks to Nakanime's own JSON API: `/api/catalog/search` for the anime id/slug and `POST /api/sources/anime` for embeds, both XOR-obfuscated (key = `"nkapiv1" + path`, see `NakanimeCodec`). The internal `episode_id` only exists in the Cloudflare-gated anime page's `<script id="anime-data">` JSON.
   - **Cloudflare clearances (shared by all providers)** — `CloudflareClearanceStore` (service) keeps a per-host `CloudflareClearance` (cookie + User-Agent) in `SettingManager`. `ProviderHttpClient` consults it on every request and auto-injects `Cookie`/`User-Agent` for matching hosts (a stored host covers its subdomains), so a provider needs no cookie code of its own. `ProviderHttpClient` also has raw-byte accessors (`getBytes`/`postJsonBytes`) for obfuscated bodies. `CloudflareChallengeHost` (mounted in `App.kt`) auto-solves a Cloudflare block reported by a provider for a host in `CloudflareHosts.kt`, opening the host in the in-app WebView and capturing the HttpOnly `cf_clearance` when it appears. Users can also capture it explicitly via **Settings → Cloudflare** (`CloudflareClearanceScreen`, one row per `cloudflareSites` entry): the in-app WebView reads the HttpOnly `cf_clearance` plus the engine's **real** User-Agent — Android/iOS system WebView, **desktop system WebView via Nucleus Tao** (`WebViewPlatform` expect/actual) — with a manual paste fallback (the only option on wasm, where SOP/HttpOnly block cookies; the browser UA is auto-filled there). **Never pin `customUserAgentString` to a desktop-Chrome UA** (e.g. `DEFAULT_USER_AGENT`): Cloudflare Turnstile fingerprints the engine, so a Chrome UA on WebKit2GTK spins and fails forever. `WebViewUserAgent.kt` reads the engine UA via JS (`navigator.userAgent`, un-quoting the JSON-serialized result) and both `CloudflareChallengeHost` and `CloudflareClearanceScreen` store that, since Cloudflare binds `cf_clearance` to it and `ProviderHttpClient` replays it. Two more traps that silently break the replay: the desktop `WebViewCookieManager()` **must be `state.cookieManager`** (only that instance is attached to the native WebView — a fresh one's `getCookies()` returns an empty list), and `createProviderHttpClient()`'s `defaultRequest` must use `headers.appendIfNameAbsent(...)`, not `header(...)` — Ktor runs `DefaultRequest` after the request is built, so `header()` comma-joins its value after the clearance UA (`"<clearance UA>,<chrome UA>"`), which Cloudflare rejects with 403.
   - `StreamPlayability` (commonMain) — per-platform stream filtering. `currentStreamPlatform` (expect/actual: `Web` in wasmJsMain, `Native` in nonJsMain) gates `StreamPlayability.platformsFor(url)`, keyed on the CDN host. `EpisodeDetailViewModel` only exposes streams playable on the current target and auto-selects the first playable one. The domain lists (`ALL_HOSTS` = verified playable everywhere: vmpx.online/vmcld.space/vmbox.space/vivibebe.site/vibevibe.workers.dev; `NATIVE_HOSTS` = direct-file CDNs mp4upload.com/okcdn.ru/my.mail.ru; `DEAD_HOSTS` = krussdomi.com/animeparadise.moe/anime-sama.fr/sibnet.ru; plus ephemeral `master.txt` worker streams and `/troll/` embed placeholders) were verified by browser-testing the wasm build and rotate as the CDNs change — re-audit when streams start failing.
4. `di/Koin.kt` (commonMain) + platform `Koin.*.kt` files — wire `JikanClient`, `SettingManager`, repositories, providers, and ViewModels via a `platformModule` expect/actual.

**Navigation:** Voyager with `Navigator` root in `App.kt`. Screens are `object` or `class` instances pushed onto the Voyager stack.

**State:** `MutableStateFlow` in ViewModels, collected in Composables with `collectAsStateWithLifecycle()`. Pagination handled by `lazy-pagination-compose`'s `PaginationState`.

**Settings persistence:** `multiplatform-settings` with platform adapters — `SharedPreferences` (Android), `NSUserDefaults` (iOS), `java.util.prefs` (Desktop), `StorageSettings` (WasmJS). Accessed via `SettingManager` service (theme, locale, playback preferences, watch history).

**i18n:** Lyricist library. All user-visible strings live in `i18n/EnStrings.kt` and `i18n/FrStrings.kt`. `Strings.kt` defines the data class hierarchy. Use `val s = strings.someSection` inside composables; `strings` comes from `LocalStrings.current`.

**Images:** Coil 3 with Ktor3 network engine. Use `AsyncImage` from `coil3.compose`.

**Video playback:** MediaMP (`mediamp-all`, ExoPlayer/MPV/AVKit/HTMLVideo backends). The desktop (MPV) backend needs its native runtime on the classpath: `runtimeOnly(libs.mediamp.mpv.runtime)` is wired in `desktopApp` — without it the player fails at startup with "mpv native runtime not found". `StreamingVideoPlayer` composable renders the `MediampPlayerSurface` + a hand-built `PlayerControlBar`; `PlayerFullscreenEffect` (expect/actual per platform) handles native window fullscreen.

**Web playback & CORS proxy (wasm — dev-only, see "Target status"):** on `wasmJs` the player is `VideoPlayer.wasmJs.kt` (native `HTMLVideoElement` via `HtmlElementView` + hls.js for HLS; non-JS targets use MediaMP in `VideoPlayer.nonJs.kt`). Cross-origin media is fetched through a **local dev-server proxy**: `webApp/webpack.config.d/0-media-proxy.js` registers a `GET|POST|OPTIONS /proxy?url=<target>` middleware on webpack-dev-server. It fetches server-side (residential IP, so anime CDNs don't block it like they block public CORS proxies), streams with permissive CORS headers, supports Range/206 (seeking) and POST (scraping), and rewrites HLS manifests so every relative URI becomes an absolute `/proxy?url=<abs>` URL (fixes root-relative/query-only playlists). `ProviderHttp.wasmJs.kt` routes all wasm proxying (media via `proxyMediaUrl`, scraping/subtitles via `CORS_PROXIES`) through this local proxy first, with public proxies (`proxy.cors.sh`, `proxy.corsfix.com`) as fallback. The proxy only exists in the dev server — a deployed static site needs its own backend for media. Targets embedded as proxy query values MUST be fully percent-encoded (`encodeUrlComponent` in `ProviderHttp.wasmJs.kt`) — Ktor's `encodeURLParameter` keeps `?&=` unescaped, which truncates the target URL at its own query (dropping HLS tokens like vmpx `?t=`).

**Logging:** Kermit. Initialize per platform (e.g. `initKermitLogging()` in desktop `main.kt`); `KermitKoinLogger` bridges Koin logging to Kermit.

## Design System

All spacing, sizing, corner radius, elevation, and responsive breakpoint values are defined as token objects in `ui/theme/` (`Spacing.kt`, `Size.kt`, `CornerRadius.kt`, `Elevation.kt`, `Breakpoints.kt`):

```kotlin
object Spacing   { xs, sm, md, lg }
object Size      { iconSm, progressSm, progressMd, imageCollapsed, imageExpanded, maxContentWidth }
object CornerRadius { sm, md }
object Elevation { sm, md, lg }
object Breakpoints { compactMaxWidth, tabletMaxWidth }
```

`Breakpoints` holds the responsive window-width thresholds (`compactMaxWidth` = 760dp, `tabletMaxWidth` = 1024dp). Compare against `BoxWithConstraints` `maxWidth` (`val compact = maxWidth < Breakpoints.compactMaxWidth`) instead of hard-coding widths.

`Shape.kt` applies `CornerRadius` tokens globally to the M3 `Shapes` theme, so components inherit rounded corners without explicit `shape` parameters.

`seamlessInputColors()` in `InputStyle.kt` — shared `TextFieldColors` that blends `TextField` into a `surfaceContainerHighest` surface (transparent indicators, matching container color). Always use this for `TextField`.

## Key Dependency Versions

| Library | Version |
|---|---|
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.12.0 |
| Koin | 4.2.2 |
| Voyager | 2.2.21-1.10.3 |
| Ktor | 3.5.2 |
| jikan4k | 1.0.3 |
| Coil | 3.6.3 |
| lazy-pagination-compose | 1.7.3 |
| Lyricist | 1.9.0 |
| mediamp | 0.5.0 |
| Kermit | 2.1.0 |
| composewebview (NucleusFramework) | 1.0.3 |
| nucleus | 2.5.5 |

## Opt-in Annotations

The following are enabled project-wide in `build.gradle.kts` and can be used without per-file `@OptIn`:
- `ExperimentalComposeUiApi`
- `ExperimentalSettingsApi`
- `ExperimentalMaterial3Api`

## Video player — ComposeMediaPlayer (desktop video on Tao)

Desktop/native playback uses [`kdroidFilter/ComposeMediaPlayer`](https://github.com/kdroidFilter/ComposeMediaPlayer)
(`io.github.kdroidfilter:composemediaplayer`), which replaced the earlier mediamp integration.

It works on Nucleus **Tao** because it decodes natively (GStreamer `playbin` on Linux, platform
bridges on macOS/Windows) but renders **offscreen into a Compose `Canvas`** — no
`SwingPanel`/`LocalAwtWindow`/`SkiaLayer`, and no need for the host window's Skia
`DirectContext`. Its native libraries ship inside the JVM artifact and are extracted to a cache
by the library's own loader.

Where things live:
- `shared/src/nonJsMain/.../player/VideoPlayer.nonJs.kt` — the `StreamingVideoPlayer` actual.
  (`VideoPlayer.wasmJs.kt` is a separate HTMLVideo/hls.js player and is untouched.)
- `shared/src/nonJsMain/.../player/PlayerControlBar.kt` — control bar over `VideoPlayerState`.
- `third_party/composemediaplayer-native-linux/` — a **patched** `libNativeVideoPlayer.so`
  plus its sources, README and build script. Upstream's frame path races its own reader (the
  GStreamer thread memcpy's into, and `free()`s, the buffer the Compose side is copying), which
  caused visible tearing/artifacts; the patch triple-buffers the frame publish and keeps slots
  grow-only. `desktopApp` puts it first on `java.library.path`, which wins because the library's
  loader tries `System.loadLibrary` before extracting its bundled copy.

Deliberately not used: the library's own `toggleFullscreen()`/fullscreen `Window` (AWT, and on
Linux the surface stops painting while `VideoPlayerState.isFullscreen` is set). Fullscreen stays
driven by the app through `onFullscreenChange`.

Known gaps:
- **`dispose()` is asynchronous** — the library tears the native player down on a background
  thread, so switching streams briefly overlaps the previous stream's audio.
- **`openUri(uri)` takes no headers**, so providers' `StreamSource.headers` are dropped. The
  patched `.so` derives a `Referer` from the media URL for non-HLS sources; per-stream headers
  are not forwarded.
- No runtime audio-track selection (the library takes an `AudioMode` at construction), so the
  audio selector is gone.
- Linux needs system GStreamer (`gstreamer1.0-plugins-{base,good,bad,libav}`).
- Only Linux x86-64 is patched/built. The packaged Linux app ships it (see
  `third_party/composemediaplayer-native-linux/README.md`); other platforms and architectures
  fall back to the library's bundled `.so`.

## Target status

**Shipping targets are Desktop (JVM), Android and iOS. WasmJS is frozen as a dev/preview target — do not treat it as a deployment target, and don't add provider/platform feature work that only exists to serve it.**

The app's core is a cross-origin scraper plus a media player, and the web platform blocks both directly: provider pages/CDNs don't send CORS headers, and Cloudflare's `cf_clearance` is HttpOnly (and Same-Origin Policy hides other origins' cookies) so it can only be supplied by manual paste on wasm. Locally this is bridged by the dev-server proxy (`webApp/webpack.config.d/0-media-proxy.js`, `GET/POST /proxy?url=`), which is an **open proxy** and exists only in the dev server — a shipped web build would need a hardened backend (allowlisted hosts, no arbitrary URLs, quotas, signed short-lived media URLs). The native targets need none of that: direct HTTP, native WebView cookie access, ComposeMediaPlayer.

So keep the wasm target *compiling* — it's a fast browser harness for shared Compose UI and for auditing `StreamPlayability` CDN hosts — but build new work against the native targets.

## Platform Targets

- Desktop JVM targets JVM 17 (`jvm("desktop")` in `shared`; `desktopApp` is a plain JVM 17 module).
- Android targets JVM 11 (configured via `kotlin.androidLibrary.compilerOptions` in `shared` and `kotlin.target.compilerOptions` in `androidApp`).

## Design source of truth = OpenDesign

The app's design (handoff spec + interactive prototype) lives in **OpenDesign**, NOT in this repo. Access it through the `open-design` MCP (daemon at `127.0.0.1:7456`):

- **Project:** `Animu Finder` (id `animu-finder`)
- **Design spec:** `design/DESIGN.md` — the handoff contract. Pins both color schemes (light/dark), Inter typography, shapes, layout gutters, the component map (prototype → composable file), and behavioral rules (on-image dark-glass CTAs, banner bottom fade, motion/reduced-motion, 44px touch targets, empty/loading states, keyboard operability).
- **Prototype:** `animu-finder-v2.html` — the interactive HTML prototype (Home hero + shelves, Browse search/filters, per-title Detail, Episode player + shelf, My List, Settings). Authoritative visual reference: when a screen changes here, port the change per the component map in `design/DESIGN.md`.
- Read with `get_file` / `get_artifact` (project defaults to `animu-finder`); locate strings/classes with `search_files`; edit via `write_file`.
- `ui/theme/Color.kt` (and `Type.kt`/`Shape.kt`) must be aligned to the schemes in `design/DESIGN.md` — that file is the token contract, not the legacy mockup.
- The legacy extraction mockup `design/mockups/layout-mockups.html` is archived; do not treat it as current.

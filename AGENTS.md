# AGENTS.md

This file provides guidance to AI coding agents (OpenCode, Claude Code, Codex, Cursor, etc.) when working with code in this repository.

## Build & Run Commands

```shell
# Desktop
./gradlew :desktopApp:run

# Web (WASM)
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
- `desktopApp` — pure JVM application module (`org.jetbrains.kotlin.jvm` + Compose). Hosts the desktop `main.kt` entry point and the `compose.desktop {}` packaging config; depends on `shared`.
- `webApp` — pure WasmJS application module (`kotlinMultiplatform` + Compose). Hosts the web `main.kt` entry point, webpack config, and `index.html`; depends on `shared`.

**Layers:**
1. `navigation/screen/` — Voyager `Screen` implementations, own their `@Composable Content()`. Screens pull ViewModels via `koinViewModel()`.
2. `viewmodel/` — `androidx.lifecycle.ViewModel` subclasses, expose `StateFlow`s.
3. `repository/` — data access layer:
   - `AnimeRepository.kt` — wraps `JikanClient` (jikan4k library, Tenrai API); returns `ResponseState` sealed type (`Loading / Success / Error / None`).
   - `provider/` — video streaming. `StreamingProvider` interface → `BaseProvider` (shared title/episode matching via Levenshtein similarity) → per-provider implementations (`VoirAnimeProvider`, `AnimeSamaProvider`, `AnimePaheProvider`, `AnimeYaProvider`). `StreamRepository` fans out provider extraction concurrently (`callbackFlow`). `ProviderHttp`/`ProviderHttpClient` use expect/actual per platform.
4. `di/Koin.kt` (commonMain) + platform `Koin.*.kt` files — wire `JikanClient`, `SettingManager`, repositories, providers, and ViewModels via a `platformModule` expect/actual.

**Navigation:** Voyager with `Navigator` root in `App.kt`. Screens are `object` or `class` instances pushed onto the Voyager stack.

**State:** `MutableStateFlow` in ViewModels, collected in Composables with `collectAsStateWithLifecycle()`. Pagination handled by `lazy-pagination-compose`'s `PaginationState`.

**Settings persistence:** `multiplatform-settings` with platform adapters — `SharedPreferences` (Android), `NSUserDefaults` (iOS), `java.util.prefs` (Desktop), `StorageSettings` (WasmJS). Accessed via `SettingManager` service (theme, locale, playback preferences, watch history).

**i18n:** Lyricist library. All user-visible strings live in `i18n/EnStrings.kt` and `i18n/FrStrings.kt`. `Strings.kt` defines the data class hierarchy. Use `val s = strings.someSection` inside composables; `strings` comes from `LocalStrings.current`.

**Images:** Coil 3 with Ktor3 network engine. Use `AsyncImage` from `coil3.compose`.

**Video playback:** MediaMP (`mediamp-all`, ExoPlayer/MPV/AVKit/HTMLVideo backends). The desktop (MPV) backend needs its native runtime on the classpath: `runtimeOnly(libs.mediamp.mpv.runtime)` is wired in `desktopApp` — without it the player fails at startup with "mpv native runtime not found". `StreamingVideoPlayer` composable renders the `MediampPlayerSurface` + a hand-built `PlayerControlBar`; `PlayerFullscreenEffect` (expect/actual per platform) handles native window fullscreen.

**Logging:** Kermit. Initialize per platform (e.g. `initKermitLogging()` in desktop `main.kt`); `KermitKoinLogger` bridges Koin logging to Kermit.

## Design System

All spacing, sizing, corner radius, and elevation values are defined as token objects in `ui/theme/` (`Spacing.kt`, `Size.kt`, `CornerRadius.kt`, `Elevation.kt`):

```kotlin
object Spacing   { xs, sm, md, lg }
object Size      { iconSm, progressSm, progressMd, imageCollapsed, imageExpanded, maxContentWidth }
object CornerRadius { sm, md }
object Elevation { sm, md, lg }
```

`Shape.kt` applies `CornerRadius` tokens globally to the M3 `Shapes` theme, so components inherit rounded corners without explicit `shape` parameters.

`seamlessInputColors()` in `InputStyle.kt` — shared `TextFieldColors` that blends `TextField` into a `surfaceContainerHighest` surface (transparent indicators, matching container color). Always use this for `TextField` and `SearchDropdown`.

## Key Dependency Versions

| Library | Version |
|---|---|
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1 |
| Koin | 4.2.2 |
| Voyager | 2.2.21-1.10.3 |
| Ktor | 3.5.2 |
| jikan4k | 1.0.3 |
| Coil | 3.5.0 |
| lazy-pagination-compose | 1.7.3 |
| Lyricist | 1.9.0 |
| mediamp | 0.3.0 |
| Kermit | 2.1.0 |

## Opt-in Annotations

The following are enabled project-wide in `build.gradle.kts` and can be used without per-file `@OptIn`:
- `ExperimentalComposeUiApi`
- `ExperimentalSettingsApi`
- `ExperimentalMaterial3Api`

## Platform Targets

- Desktop JVM targets JVM 17 (`jvm("desktop")` in `shared`; `desktopApp` is a plain JVM 17 module).
- Android targets JVM 11 (configured via `kotlin.androidLibrary.compilerOptions` in `shared` and `kotlin.target.compilerOptions` in `androidApp`).

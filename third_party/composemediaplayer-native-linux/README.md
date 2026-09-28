# Patched `libNativeVideoPlayer.so` (ComposeMediaPlayer, Linux)

`io.github.kdroidfilter:composemediaplayer` bundles a prebuilt Linux GStreamer player
(`NativeVideoPlayer.c`) and reads frames offscreen into a Compose `Canvas`. Two defects in
that native code show up badly in this app; this directory carries a patched build.

Sources are the upstream `v0.11.4` files from `mediaplayer/src/jvmMain/native/linux/` (fetched
unchanged, then patched). `dist/linux/libNativeVideoPlayer.so` is the built artifact the app
and the packaging use.

## Patch 1 — frame-buffer race (visual artifacts)

**Upstream bug.** `on_new_sample` (the GStreamer streaming thread) memcpy's each frame into a
single shared `p->frame_buffer`, and `free()`+`malloc()`s it whenever the output size changes.
The reader (`LinuxVideoPlayerState.updateFrameAsync`) reads `p->frame_buffer` through
`nvp_get_latest_frame_address` and copies it **without any lock**. So the reader copies a
half-written frame (tearing), and after any output-size change — startup, window resize,
`nvp_set_output_size` renegotiation — it can copy freed memory.

**Fix.** Triple-buffered publish: the writer fills a slot other than the published one and then
publishes it with `__atomic_store_n(..., __ATOMIC_RELEASE)`; `nvp_get_latest_frame_address`
loads it with `__ATOMIC_ACQUIRE`, and dimensions are stored before the pointer. Slots are
grow-only (never shrunk, and the written slot is never the published one), so a reader that
races a resize can no longer touch freed memory. The Kotlin-side JNI API is unchanged.

Also kept: `update_stream_metadata` still seeds the dimensions from the appsink caps, because
`LinuxVideoPlayerState.pollDimensionsUntilReady` (up to 5s) waits for non-zero dimensions
before playback starts. Removing that stalls playback entirely.

## Patch 2 — Referer for hotlink-protected sources

`VideoPlayerState.openUri(uri)` carries no headers, but providers pass `StreamSource.headers`
(in practice a `Referer`) — mediamp used to forward those. The patched `on_source_setup`
handler on `playbin` attaches `extra-headers` (`Referer` = the media URL's own directory, plus
a browser `User-Agent`) to plain HTTP sources. HLS is skipped: it is downloaded by
`hlsdemux`'s internal source, which `source-setup` does not cover, and the HLS CDNs used here
need no Referer.

**Not verified to fix anything specific**: sibnet turns out to be unusable regardless (see
below). Kept because it restores the Referer contract that was lost with mediamp; revisit if a
direct-file CDN misbehaves.

## Patch 3 — build output directory

`CMakeLists.txt` emits to `<NATIVE_LIBS_OUTPUT_DIR>/linux` (upstream appends an arch suffix)
so the same tree doubles as Nucleus's `appResourcesRootDir`, whose `linux/` subdirectory
(`JvmOs.Linux.id`) is copied into the packaged app's resources directory.

## Deliberately not fixed here

`dispose()` tears the native player down on a **background thread**
(`LinuxVideoPlayerState.dispose`), so switching streams overlaps the old pipeline's audio with
the new one for a moment. That is Kotlin-side (library source), not fixable from the `.so`.

## Build

```shell
cd native
NATIVE_LIBS_OUTPUT_DIR=/abs/path/to/dist bash build.sh
# -> dist/linux/libNativeVideoPlayer.so
```

Needs `libgstreamer1.0-dev` + `libgstreamer-plugins-base1.0-dev`, CMake, a C compiler and JNI
headers (`JAVA_HOME`). One architecture per build: overwrite `dist/linux/` with the build for
the arch you are packaging.

## How the app picks it up

The library's `NativeLibraryLoader` tries `System.loadLibrary("NativeVideoPlayer")` — which
searches `java.library.path` — **before** extracting the copy bundled in the jar. So
`desktopApp/build.gradle.kts` puts this build on `java.library.path` for both cases (Linux only;
`-PbundledVideoLib` opts out for A/B testing):

- **dev**: the `run`/`JavaExec` tasks get the absolute `dist/linux` directory.
- **packaged**: `nativeDistributions.appResourcesRootDir` ships `dist/linux/…` into the app's
  resources (`lib/app/resources/` on Linux) and the launcher sets
  `-Djava.library.path=$APPDIR/resources:…`, which jpackage resolves to that directory.

Verify a packaged build with
`jcmd <pid> VM.command_line | grep java.library.path` (no need to reach the player) and
`grep libNativeVideoPlayer /proc/<pid>/maps` once a player has been opened.

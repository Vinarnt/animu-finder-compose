# Patched `libNativeVideoPlayer.so` (ComposeMediaPlayer, Linux)

`io.github.kdroidfilter:composemediaplayer` bundles a prebuilt Linux GStreamer player
(`NativeVideoPlayer.c`) and reads frames offscreen into a Compose `Canvas`. Two defects in that
native code show up badly in this app, so this directory carries a patched build.

Sources are the upstream `v0.11.4` files from `mediaplayer/src/jvmMain/native/linux/`, fetched
unchanged and then patched. `dist/linux/libNativeVideoPlayer.so` is the artifact the app and the
packaging use.

## Patch 1: frame-buffer race (visual artifacts)

**Upstream bug.** `on_new_sample` (the GStreamer streaming thread) memcpy's each frame into a
single shared `p->frame_buffer`, and `free()`+`malloc()`s it whenever the output size changes. The
reader (`LinuxVideoPlayerState.updateFrameAsync`) reads `p->frame_buffer` through
`nvp_get_latest_frame_address` and copies it with no lock. The reader can therefore copy a
half-written frame (tearing), and after an output-size change (startup, window resize,
`nvp_set_output_size` renegotiation) it can copy freed memory.

**Fix.** Triple-buffered publish: the writer fills a slot other than the published one, then
publishes it with `__atomic_store_n(..., __ATOMIC_RELEASE)`; `nvp_get_latest_frame_address` loads
it with `__ATOMIC_ACQUIRE`, and dimensions are stored before the pointer. Slots are grow-only and
the written slot is never the published one, so a reader racing a resize cannot touch freed
memory. The Kotlin-side JNI API is unchanged.

`update_stream_metadata` still seeds the dimensions from the appsink caps, because
`LinuxVideoPlayerState.pollDimensionsUntilReady` (up to 5s) waits for non-zero dimensions before
playback starts. Removing that stalls playback entirely.

## Patch 2: Referer for hotlink-protected sources

`VideoPlayerState.openUri(uri)` carries no headers, but providers pass `StreamSource.headers` (in
practice a `Referer`), which mediamp used to forward. The patched `on_source_setup` handler on
`playbin` attaches `extra-headers` (`Referer` = the media URL's own directory, plus a browser
`User-Agent`) to plain HTTP sources. HLS is skipped: `hlsdemux` downloads it through its own
internal source, which `source-setup` does not cover, and the HLS CDNs used here need no Referer.

This patch is not known to fix anything specific; sibnet turned out to be unusable either way (see
below). It is kept because it restores the Referer contract that was lost with mediamp. Revisit it
if a direct-file CDN misbehaves.

## Patch 3: build output directory

`CMakeLists.txt` emits to `<NATIVE_LIBS_OUTPUT_DIR>/linux` instead of upstream's arch-suffixed
directory, so the tree doubles as Nucleus's `appResourcesRootDir`: its `linux/` subdirectory
(`JvmOs.Linux.id`) is copied into the packaged app's resources directory.

## Not fixed here

`dispose()` tears the native player down on a background thread
(`LinuxVideoPlayerState.dispose`), so switching streams overlaps the old pipeline's audio with the
new one for a moment. That one is Kotlin-side, in the library, and cannot be fixed from the `.so`.

## Build

```shell
cd native
NATIVE_LIBS_OUTPUT_DIR=/abs/path/to/dist bash build.sh
# -> dist/linux/libNativeVideoPlayer.so
```

Needs `libgstreamer1.0-dev`, `libgstreamer-plugins-base1.0-dev`, CMake, a C compiler and JNI
headers (`JAVA_HOME`). One architecture per build: overwrite `dist/linux/` with the build for the
arch you are packaging.

## How the app picks it up

The library's `NativeLibraryLoader` tries `System.loadLibrary("NativeVideoPlayer")` first (so
`java.library.path`), then extracts the classpath resource
`composemediaplayer/native/<platform>/libNativeVideoPlayer.so`. `desktopApp/build.gradle.kts` adds
this build as that resource for the main source set, so the app's own resources win over the
dependency jar and the loader picks it up. Its cache check compares file sizes, so an older copy
in `~/.cache/composemediaplayer/native/<platform>/` is replaced. This behaves the same in dev and
packaged runs and needs no JVM arguments. Linux only; `-PbundledVideoLib` skips it for comparison.

To check a build: `unzip -l desktopApp/build/libs/desktopApp.jar | grep libNativeVideoPlayer`, and
after opening a player, `grep libNativeVideoPlayer /proc/<pid>/maps` shows which library was
loaded.

import dev.nucleusframework.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.nucleus)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }

    dependencies {
        implementation(projects.shared)
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.slf4j.simple)
        implementation(libs.ktor.client.java)
        // Desktop video: decodes natively (GStreamer on Linux) but draws each frame into a
        // Compose Canvas, so it runs inside the Nucleus Tao window (no AWT/SkiaLayer and no
        // DirectContext sharing). Native libs ship inside the JVM artifact; the Linux one is
        // patched locally — see third_party/composemediaplayer-native-linux.
        implementation(libs.composemediaplayer)

        // Desktop WebView uses the system engine via Nucleus Tao (WebKit2GTK / WKWebView /
        // WebView2) — no bundled browser (the old KCEF/JCEF path is gone).
        implementation(libs.composewebview)
        implementation(libs.nucleus.application)
        implementation(libs.nucleus.decorated.window.tao)
        implementation(libs.nucleus.core.runtime)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// --- Patched Linux video library -------------------------------------------------------------
// third_party/composemediaplayer-native-linux carries a patched libNativeVideoPlayer.so (its
// native frame path raced its own reader — tearing and reads of freed memory). The library's
// NativeLibraryLoader tries System.loadLibrary("NativeVideoPlayer"), which searches
// java.library.path, *before* extracting its bundled copy — so putting the patched build on
// java.library.path wins in both dev and packaged runs:
//   * dev: the run/JavaExec tasks get the absolute directory (added in doFirst, after all other
//     configuration, so it is the -Djava.library.path that takes effect).
//   * packaged: appResourcesRootDir ships dist/linux/… into the app's resources directory and
//     the launcher's java.library.path points at $APPDIR/resources.
// Linux-only (the patched .so is only built for Linux; jpackage packages for the host OS).
// Pass -PbundledVideoLib to A/B against the library's bundled .so.
val isLinux = System.getProperty("os.name").lowercase().contains("linux")
val usePatchedVideoLib = project.findProperty("bundledVideoLib") == null
val patchedVideoLibDist = rootProject.layout.projectDirectory
    .dir("third_party/composemediaplayer-native-linux/dist")
val patchedVideoLibDir = patchedVideoLibDist.dir("linux")

if (usePatchedVideoLib) {
    tasks.withType<JavaExec>().configureEach {
        doFirst {
            jvmArgs(
                "-Djava.library.path=${patchedVideoLibDir.asFile.absolutePath}:" +
                    "/usr/java/packages/lib:/usr/lib/jni:/lib:/usr/lib",
            )
        }
    }
}

// Nucleus packaging (replaces compose.desktop.application): the Tao window + native WebView
// need Nucleus's runtime, and its distribution DSL knows about the WebKit2GTK dependency.
nucleus.application {
    mainClass = "fr.vinarnt.animu.finder.compose.MainKt"

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb)
        appName = "Animu Finder"
        packageName = "fr.vinarnt.animu.finder.compose"
        packageVersion = "1.0.0"
        // electron-builder (the Deb backend) requires a homepage; description/vendor round out
        // the Debian control file.
        homepage = "https://github.com/Vinarnt/animu-finder-compose"
        description = "Anime discovery and streaming aggregator (Desktop, Android, iOS)."
        vendor = "Vinarnt"

        if (usePatchedVideoLib && isLinux) {
            // $APPDIR is substituted by the launcher; the `linux/` subdir of this root
            // (JvmOs.Linux.id) is copied into the app's resources dir.
            appResourcesRootDir.set(patchedVideoLibDist)
            jvmArgs("-Djava.library.path=\$APPDIR/resources:/usr/java/packages/lib:/lib:/usr/lib")
        }

        linux {
            debMaintainer = "Vinarnt <vinarnt@outlook.com>"
        }

        macOS {
            bundleID = "fr.vinarnt.animu.finder.compose"
        }
    }
}

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
        implementation(libs.composemediaplayer)
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
// third_party/composemediaplayer-native-linux carries a patched libNativeVideoPlayer.so. Its
// native frame path raced its own reader, which showed up as tearing and reads of freed memory.
// The library's NativeLibraryLoader first tries `System.loadLibrary` (so java.library.path),
// then extracts this classpath resource. Shipping the patched build as that resource works the
// same in dev and packaged runs: the app's own resources come before the dependency jar on the
// classpath, so `getResource` finds ours, and the loader's size check re-extracts it over a
// stale copy. No JVM arguments involved.
// Linux only, since the patched .so is built for Linux.
// Pass -PbundledVideoLib to compare against the library's bundled .so.
val isLinux = System.getProperty("os.name").lowercase().contains("linux")
val usePatchedVideoLib = project.findProperty("bundledVideoLib") == null
val videoLibPlatform =
    if (System.getProperty("os.arch").lowercase().let { it.contains("aarch64") || it.contains("arm") }) {
        "linux-aarch64"
    } else {
        "linux-x86-64"
    }
val patchedVideoLibDir = rootProject.layout.projectDirectory
    .dir("third_party/composemediaplayer-native-linux/dist/linux")

if (usePatchedVideoLib && isLinux) {
    tasks.named<Copy>("processResources") {
        from(patchedVideoLibDir) { into("composemediaplayer/native/$videoLibPlatform") }
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

        linux {
            debMaintainer = "Vinarnt <vinarnt@outlook.com>"
        }

        macOS {
            bundleID = "fr.vinarnt.animu.finder.compose"
        }
    }
}

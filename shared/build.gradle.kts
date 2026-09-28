import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn.add("com.russhwolf.settings.ExperimentalSettingsApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }

    androidLibrary {
        namespace = "fr.vinarnt.animu.finder.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        androidResources {
            enable = true
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "DebugProbesKt.bin"
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Animu Finder"
            isStatic = true
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // Frozen dev/preview target (see AGENTS.md → "Target status"). Kept compiling so
        // the shared Compose UI can be checked in a browser; not a shipping target.
    }

    sourceSets {
        val commonMain by getting
        val desktopMain by getting
        val wasmJsMain by getting

        val nonJsMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.persistent.cache)
                // Desktop/native/mobile video playback. Renders decoded frames into a
                // Compose Canvas on desktop, so it works on Nucleus/Tao (no AWT/SkiaLayer).
                // Native libs ship inside the JVM artifact — no runtime download.
                implementation(libs.composemediaplayer)
            }
        }

        androidMain {
            dependsOn(nonJsMain)
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.material3.android)
                implementation(libs.koin.android)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
            }
        }
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.preview)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.ktor.client.logging)
                implementation(libs.koin.core)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
                implementation(libs.bundles.voyager)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.serialization)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.multiplatform.settings.make.observable)
                implementation(libs.lyricist)
                implementation(libs.bundles.coil)
                implementation(libs.jikan4k)
                implementation(libs.lazyPaginationCompose)
                api(libs.composewebview)
            }
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.slf4j.simple)
            implementation(libs.ktor.client.java)
        }
        desktopMain.dependsOn(nonJsMain)

        iosMain {
            dependsOn(nonJsMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        wasmJsMain.dependencies {
            implementation(npm("hls.js", "1.7.1"))
        }
    }
}

dependencies {
    androidRuntimeClasspath(compose.uiTooling)
}

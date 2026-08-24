import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
        // The MPV backend is bundled by mediamp-all but its native runtime (libmpv +
        // FFmpeg) ships separately; without it the player fails to start on desktop.
        runtimeOnly(libs.mediamp.mpv.runtime)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

compose.desktop {
    application {
        mainClass = "fr.vinarnt.animu.finder.compose.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "fr.vinarnt.animu.finder.compose"
            packageVersion = "1.0.0"
        }
    }
}

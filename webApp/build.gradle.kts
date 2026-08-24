import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn.add("com.russhwolf.settings.ExperimentalSettingsApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "webApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "webApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(compose.ui)
        }
    }
}

package fr.vinarnt.animu.finder.compose.model

/**
 * Target platform a stream can run on. [Web] is the wasmJs build, [Native] covers
 * desktop / Android / iOS.
 */
enum class StreamPlatform { Web, Native }

/** The platform of the current build target. */
expect val currentStreamPlatform: StreamPlatform
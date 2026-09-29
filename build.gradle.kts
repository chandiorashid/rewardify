// Top-level build file for the Rewardify project.
// Kotlin 2.2.20: the Kotlin compiler must be >= the Kotlin version the newest
// dependency was built with. The ad SDKs pull AndroidX (compose 1.9.0,
// lifecycle 2.10.0, ...) built with Kotlin 2.1/2.2 metadata, which the old
// 2.0.21 compiler could not read and crashed on (FirIncompatibleClassExpressionChecker ICE).
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    // Required since Kotlin 2.0: the Compose compiler now lives in this
    // separate plugin instead of the Kotlin Gradle plugin.
    // Its version must always match the Kotlin version.
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
}

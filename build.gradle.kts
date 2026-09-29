// Top-level build file for the Rewardify project.
// Kotlin 2.4.20: the Kotlin compiler must be >= the Kotlin version the newest
// dependency was built with. Supabase 3.8.0 ships Kotlin 2.4 metadata, which
// older compilers cannot read (surfaces as a cascade of "unresolved
// reference" errors). The ad SDKs also pull AndroidX (compose 1.9.0,
// lifecycle 2.10.0, ...) built with Kotlin 2.1/2.2 metadata.
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    // Required since Kotlin 2.0: the Compose compiler now lives in this
    // separate plugin instead of the Kotlin Gradle plugin.
    // Its version must always match the Kotlin version.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
    kotlin("plugin.serialization") version "2.4.20" apply false
}

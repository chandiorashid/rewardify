// Top-level build file for the Rewardify project.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Required since Kotlin 2.0: the Compose compiler now lives in this
    // separate plugin instead of the Kotlin Gradle plugin.
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.milesolutions.rewardify"
    // compileSdk 36: required by AndroidX libraries pulled in transitively
    // by the ads SDKs (browser 1.10.0, compose 1.9.0, ...). targetSdk stays
    // 34 on purpose — raising compileSdk alone changes no runtime behavior.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.milesolutions.rewardify"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // Supabase (auth + database + storage)
    val supabaseBom = platform("io.github.jan-tennert.supabase:bom:3.8.0")
    implementation(supabaseBom)
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    // Ktor HTTP engine (required): Supabase 3.x is built on Ktor, and Ktor
    // ships without a default engine. Without this, the app crashes on
    // launch with "Failed to find HTTP client engine implementation".
    // 3.5.1 matches the Ktor version the Supabase 3.8.0 BOM resolves.
    implementation("io.ktor:ktor-client-okhttp:3.5.1")

    // Google Mobile Ads (rewarded ads). Version pinned to a known-good
    // release — bump deliberately, then re-test ad loading.
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    // Meta Audience Network mediation adapter. Version 6.19.0.0 is the
    // newest adapter built AND tested with GMA 23.6.0 (per Google's
    // changelog); 6.19.0.1+ requires GMA 24.0.0+, so don't bump this
    // without also bumping play-services-ads. Pulls the FAN SDK
    // transitively. Mediation itself is configured server-side in the
    // AdMob dashboard (mediation group), not in code.
    implementation("com.google.ads.mediation:facebook:6.19.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

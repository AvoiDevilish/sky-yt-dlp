plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.skyytdlp.poc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skyytdlp.poc"
        // UNKNOWN — NEEDS LOCAL VERIFICATION: youtubedl-android's actual
        // minimum supported SDK was not confirmed against its published
        // AAR manifest. 24 is a conservative modern default; raise it if
        // Gradle's manifest merger complains.
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-poc"

        ndk {
            // arm64-v8a: primary physical-device target (per mission).
            // x86_64: emulator testing only.
            // armeabi-v7a / x86 intentionally excluded to keep this PoC
            // small; the library's own README lists all four as supported
            // if broader device coverage is needed later.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    // youtubedl-android's README instructs setting
    // android:extractNativeLibs="true" in the manifest so its bundled
    // yt-dlp/Python native payload is extracted to disk rather than run
    // from inside the APK. This packaging block is the AGP 8.x equivalent
    // knob. UNKNOWN — NEEDS LOCAL VERIFICATION: whether both need to agree,
    // or one supersedes the other, on AGP 8.5 specifically.
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
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

    composeOptions {
        // Must be compatible with the Kotlin version pinned in the root
        // build.gradle.kts (1.9.24). NEEDS LOCAL VERIFICATION against
        // whatever Android Studio/AGP version you actually build with.
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Core PoC dependency: real yt-dlp execution engine.
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")

    // FFmpeg module intentionally NOT included in this PoC.
    // See MISSION_001C_REPORT.md, "FFmpeg decision".
    // implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
}

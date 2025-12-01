plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "com.wilkins.safezone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wilkins.safezone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ----- SUPABASE -----
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.4")

    // ----- Serialización -----
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ----- Corrutinas -----
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ----- Compose -----
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // ----- ViewModel -----
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ----- Retrofit -----
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Necesario para ComponentActivity + setContent
    implementation("androidx.activity:activity-compose:1.9.0")

    // 🚀 *** Navigation Compose (LO QUE FALTABA) ***
    implementation("androidx.navigation:navigation-compose:2.7.7")
}

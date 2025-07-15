

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)


}

android {
    namespace = "com.example.gdghub"
    compileSdk = 36

    buildFeatures {
        compose = true // Make sure this is true
    }
    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.composeCompiler.get() // Get from version catalog
    }

    defaultConfig {
        applicationId = "com.example.gdghub"
        minSdk = 29
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0") // Or latest
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Or latest
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(platform(libs.firebase.bom))
    implementation(libs.material)
    implementation(platform("androidx.compose:compose-bom:2024.02.01")) // Or latest BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.material3:material3")
    implementation(platform("androidx.compose:compose-bom:2024.02.01")) // Or latest BOM

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Or latest

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.7") // Or latest

    // Firebase (if using Firestore)
    dependencies {
        // ... other dependencies
        implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
        implementation("com.google.firebase:firebase-ai")

        implementation("com.google.firebase:firebase-firestore-ktx") // Add this line for Firestore
        implementation(libs.firebase.storage)
        // ... other dependencies
    }
    // For Kotlin Coroutines (usually included with lifecycle-viewmodel-ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") // Or latest
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.ai)
// For Firebase await()
}

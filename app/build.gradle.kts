import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.simplechat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.simplechat"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        // --- NEW CODE STARTS HERE ---
        // 1. Create a Properties object
        val properties = Properties()

        // 2. Locate the local.properties file in the project root
        val localPropertiesFile = rootProject.file("local.properties")

        // 3. Load the file if it exists
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // 4. Extract the key (default to empty string if not found)
        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: ""

        // 5. Inject it into BuildConfig (Carefully formatted quotes)
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // 1. Android Core (Pinned to 1.15.0 to avoid API 36/Android 16 crash)
    implementation("androidx.core:core-ktx:1.15.0")

    // Lifecycle & Activity
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 2. COMPOSE BOM - Version 2024.09.00 FIXES the crash
    // This manages the versions of all Compose libraries below
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // 3. Compose Libraries (No version numbers here; BOM handles them)
    // We use strings instead of 'libs.*' to ensure the BOM takes priority
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // This will now resolve to 1.3.0+ automatically

    // Icons (Let BOM handle version)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // 4. Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google AI SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage")

// Coil (For loading images in Compose)
    implementation("io.coil-kt:coil-compose:2.6.0")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.delivery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.delivery"
        minSdk = 26  // Updated for TomTom SDK requirement
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["auth0Domain"] = "almakom.eu.auth0.com"
        manifestPlaceholders["auth0Scheme"] = "delivery"
        
        // TomTom API Key - TEMPORARILY DISABLED due to missing Maven credentials
        // buildConfigField("String", "TOMTOM_API_KEY", "\"c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse\"")
        
        // ABI filters for TomTom SDK - TEMPORARILY DISABLED
        /*
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        */
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.1")
    
    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.1")
    
    // Try with just networking libraries for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // CameraX for camera functionality
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")
    
    // Permissions handling
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    
    // Lifecycle Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Auth0 SDK
    implementation("com.auth0.android:auth0:2.+")
    
    // TomTom Android SDK - TEMPORARILY DISABLED due to missing Maven credentials
    // Uncomment these lines when TomTom SDK credentials are available
    /*
    implementation("com.tomtom.sdk:init:2.1.2")
    implementation("com.tomtom.sdk:maps:2.1.2")
    implementation("com.tomtom.sdk:routing:2.1.2")
    implementation("com.tomtom.sdk:location:2.1.2")
    */
}
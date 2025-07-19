import com.android.build.api.dsl.AaptOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinAndroidKsp)
    // Add the Google services Gradle plugin
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.signify.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.signify.app"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        // keep your .tflite and .task files uncompressed
        noCompress += listOf("tflite", "task")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.material3)


// room +ks
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)


    // CameraX core libraries
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

// Import the Firebase BoM
    // import the BOM (which brings in correct versions for all Firebase components)
    implementation(platform(libs.firebase.bom))
    // reference the KTX libraries _without_ specifying a version
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)


    // your Coroutines Play Services helper
    implementation(libs.coroutines.play.services)
// TensorFlow Lite interpreter
    implementation(libs.tensorflow.lite)

    // MediaPipe Tasks Vision (Hand Landmarker)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.mediapipe.tasks.core)

    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.13.0")

}


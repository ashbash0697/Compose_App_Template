plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
}

val packageName = "com.example.core"

android {
    namespace = packageName
    compileSdk = configs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = configs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        buildConfig = true
    }
}

ksp {
    arg("compose-destinations.codeGenPackageName", "$packageName.navigation")
}

dependencies {

    // Kotlin
    api(libs.bundles.kotlin)

    // Dependency Injection
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)

    api(libs.bundles.room)
    ksp(libs.room.compiler)

    // API calls
    api(libs.bundles.retrofit)

    api(libs.datastore.preferences)



}
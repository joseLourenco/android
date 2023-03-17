plugins {
    kotlin("kapt")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "io.homeassistant.companion.android.HomeAssistantApplication"
    compileSdk = 33

    defaultConfig {
        applicationId = "io.homeassistant.companion.android.HomeAssistantApplication"
        minSdk = 29
        targetSdk = 33
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
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
}

dependencies {
    implementation(project(":common"))
    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-android-compiler:2.45")
    implementation("androidx.car.app:app-automotive:1.2.0-beta02")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("org.apache.httpcomponents:httpcore:4.2.4")
    implementation("org.apache.httpcomponents:httpclient:4.2.5")
    implementation("org.apache.httpcomponents:httpmime:4.2.5")
    implementation("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude("org.hamcrest", "hamcrest-core")
    }
}
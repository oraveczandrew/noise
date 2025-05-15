import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android.apply {
    namespace = "com.paramsen.noise.tester"

    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.paramsen.noise.tester"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin.apply {
    compilerOptions.apply {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.7.0-alpha03")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.android.support.constraint:constraint-layout:2.2.1")
    implementation("com.google.guava:guava:33.4.8-android")

    implementation(project(":noise"))
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val VERSION_NAME = "1.1"
val VERSION_CODE = 4

android {
    namespace = "com.paramsen.noise.sample"

    signingConfigs {
        getByName("debug").apply {
            keyAlias = "qqqqqq"
            keyPassword = "qqqqqq"
            storeFile = file("debug.jks")
            storePassword = "qqqqqq"
        }
    }

    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.paramsen.noise.sample"
        minSdk = 26
        targetSdk = 36
        versionCode = VERSION_CODE
        versionName = VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    flavorDimensions("sample")
    productFlavors {
        create("devel") {
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-devel"
            dimension = "sample"
        }
        create("prod") {
            versionNameSuffix = "-prod"
            dimension = "sample"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    val coroutinesVersion = "1.10.2"
    val lifecycleVersion = "2.9.2"

    implementation(kotlin("stdlib"))
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.collection:collection:1.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")

    implementation(project(":noise"))
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
}

val VERSION_NAME = "1.1"
val VERSION_CODE = 4

android.apply {
    namespace = "com.paramsen.noise.sample"

    signingConfigs.apply {
        getByName("debug").apply {
            keyAlias = "qqqqqq"
            keyPassword = "qqqqqq"
            storeFile = file("debug.jks")
            storePassword = "qqqqqq"
        }
    }

    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig.apply {
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
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    flavorDimensions.add("sample")

    productFlavors.apply {
        create("devel").apply {
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-devel"
            dimension = "sample"
        }
        create("prod").apply {
            versionNameSuffix = "-prod"
            dimension = "sample"
        }
    }

    buildFeatures.apply {
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

dependencies.apply {
    val coroutinesVersion = "1.10.2"
    val lifecycleVersion = "2.10.0"

    implementation(kotlin("stdlib"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.collection:collection:1.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")

    implementation(project(":noise"))
}

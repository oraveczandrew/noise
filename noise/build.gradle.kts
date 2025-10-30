import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
}

group = "com.github.paramsen"

val libraryVersionName = "2.0.0"

version = libraryVersionName

android.apply {
    namespace = "com.paramsen.noise"

    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig.apply {
        aarMetadata.apply {
            minCompileSdk = 26
        }

        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = libraryVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake.apply {
                arguments.addAll(listOf("-DANDROID_STL=none", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }

    externalNativeBuild.apply {
        cmake.apply {
            path = file("CMakeLists.txt")
        }
    }

    /* verify that kissfft is initialized */
    tasks.register<Task>("verifyKissfftInitialized") {
        group = "init"
        description = "Verify that kissfft is initialized"

        if (!(File("noise/src/main/native/kissfft/kiss_fft.c").exists())) {
            throw Exception("Initialize git submodules before building (read \"development\" section in readme)")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    /* jitpack.io conf */
    /*tasks.register("sourcesJar", Jar) {
        from android.sourceSets.main.java.srcDirs
        classifier = "sources"
    }
    tasks.register("javadoc", Javadoc) {
        failOnError false
        source = android.sourceSets.main.java.sourceFiles
        classpath(+= project.files(android.getBootClasspath().join(File.pathSeparator)))
        classpath(+= configurations.compile)
    }
    tasks.register("javadocJar", Jar) {
        dependsOn javadoc
        classifier = "javadoc"
        from javadoc.destinationDir
    }
    artifacts {
        archives sourcesJar
        archives javadocJar
    }*/

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies.apply {
    androidTestRuntimeOnly("androidx.test:runner:1.7.0")
}

kotlin {
    compilerOptions.apply {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndkVersion = "29.0.14206865"

        externalNativeBuild.apply {
            cmake.apply {
                arguments.addAll(listOf("-DANDROID_STL=none", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"))
            }
        }
    }

    lint.apply {
        targetSdk = 36
    }

    testOptions.apply {
        targetSdk = 36
    }

    buildTypes.apply {
        release {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
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

        if (!(File(project.projectDir.absolutePath + "/src/main/native/kissfft/kiss_fft.c").exists())) {
            throw Exception("Initialize git submodules before building (read \"development\" section in readme)")
        }
    }

    compileOptions.apply {
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

    publishing.apply {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies.apply {
    androidTestRuntimeOnly("androidx.test:runner:1.7.0")
}

kotlin.apply {
    compilerOptions.apply {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
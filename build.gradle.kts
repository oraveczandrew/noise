plugins {
    id("com.github.ben-manes.versions") version "0.54.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:9.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0-RC")
    }
}

allprojects {

    repositories.apply {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register<Task>("readme") {
    description = "Generates the readme file from the template"

    val versionString = project.version.toString()
    val inputFile = project.file("README_TEMPLATE.md")
    val outputFile = project.file("README.md")

    doLast {
        val templateStr = inputFile.readText()
        val readmeStr = templateStr.replace("{{version}}", versionString)

        outputFile.writeText(readmeStr)

        println("README.md generated from template")
    }
}

tasks.register<Task>("release") {
    description = "Creates a new release tag"

    dependsOn(":readme")
    doLast {
        Runtime.getRuntime().exec(arrayOf("git"), arrayOf("tag", "-a", "${project(":noise").version}", "-m", "Release ${project(":noise").version}"))

        println("Successfully tagged new release (${project(":noise").version}).")
        println("Push manually!")
    }
}
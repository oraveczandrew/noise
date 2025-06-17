buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0-RC3")
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

    doLast {
        val input = File("./README_TEMPLATE.md")
        val output = File("./README.md")

        val templateStr = input.readBytes().toString()

        val version = project(":noise").version.toString()
        val readmeStr = templateStr.replace("{{version}}", version)

        file(output).writeBytes(readmeStr.toByteArray())

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
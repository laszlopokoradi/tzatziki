plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.1"
}

val versions: Map<String, String> by rootProject.extra

intellij {
    version.set(versions["intellij-version"])
    plugins.set(listOf(
        "Gherkin:${versions["gherkin"]}"
    ))
}

dependencies {
    implementation("io.cucumber:tag-expressions:4.1.0")
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}
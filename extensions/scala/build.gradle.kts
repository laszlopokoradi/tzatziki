plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.1"
}

val versions: Map<String, String> by rootProject.extra

dependencies {
    implementation(project(":common"))
    implementation(project(":plugin-tzatziki"))
}

intellij {
    version.set(versions["intellij-version"])

    plugins.set(listOf(
        "java",
        "org.intellij.scala:${versions["scala"]}",
        "Gherkin:${versions["gherkin"]}",
    ))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    buildSearchableOptions {
        enabled = false
    }
    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}
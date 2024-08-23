plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

val versions: Map<String, String> by rootProject.extra

dependencies {
    implementation(project(":common"))
    implementation(project(":extensions:java-cucumber"))
    implementation(project(":plugin-tzatziki"))

    intellijPlatform {
        intellijIdeaCommunity("${versions["idea-version"]}")
        plugins("gherkin:${versions["gherkin"]}", "cucumber-java:${versions["cucumberJava"]}")
        bundledPlugins("com.intellij.java", "org.jetbrains.kotlin")
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

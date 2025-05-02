val versions: Map<String, String> by rootProject.extra

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("${versions["intellij-version"]}")

        bundledPlugins(
            listOf(
                "com.intellij.java",
                "org.jetbrains.kotlin",
            )
        )
        plugins(
            listOf(
                "Gherkin:${versions["gherkin"]}",
                "cucumber-java:${versions["cucumberJava"]}"
            )
        )
    }

    implementation(project(":common"))
    implementation(project(":extensions:java-cucumber"))
    implementation(project(":plugin-tzatziki"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "21"
//    }

    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}

intellijPlatform {
    buildSearchableOptions = false
}
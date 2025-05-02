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

val versions: Map<String, String> by rootProject.extra

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("${versions["intellij-version"]}")

        bundledPlugins(
            listOf(
                "com.intellij.java",
            )
        )
        plugins(listOf(
            "Gherkin:${versions["gherkin"]}",
            "cucumber-java:${versions["cucumberJava"]}"
        ))
    }

    implementation(project(":common"))
    implementation(project(":plugin-tzatziki"))
}

//intellij {
//    version.set(versions["intellij-version"])
//    ))
//}

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
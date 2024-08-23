import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

val versions: Map<String, String> by rootProject.extra

dependencies {
    testImplementation(project(":plugin-tzatziki"))
    testImplementation(project(":common"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testImplementation("org.apache.logging.log4j:log4j-api:2.23.1")
    testImplementation("org.apache.logging.log4j:log4j-core:2.23.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine");

    intellijPlatform {
        intellijIdeaCommunity("${versions["idea-version"]}")
        plugins(
            "Gherkin:${versions["gherkin"]}",
            "cucumber-java:${versions["cucumberJava"]}",
            "org.intellij.scala:${versions["scala"]}",
            "com.intellij.properties:${versions["properties"]}",
            "PsiViewer:${versions["psiViewer"]}",
        )
        bundledPlugins("org.intellij.intelliLang", "com.intellij.java", "org.jetbrains.kotlin")

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
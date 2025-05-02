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

//intellij {
//    version.set("IU-2021.3.1")
//    plugins.set(listOf(
//        "Gherkin:213.5744.223",
//        "Kotlin",
//        "org.intellij.intelliLang",
//        "java",
//        "JUnit",
//        "cucumber-java:213.5744.125",
//        "com.intellij.properties:213.6461.46"
//    ))
//}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("${versions["intellij-version"]}")

        bundledPlugins(
            listOf(
                "com.intellij.java",
                "org.jetbrains.kotlin",
                "org.intellij.intelliLang",
                "JUnit",
            )
        )

        plugins (
            listOf(
                "Gherkin:${versions["gherkin"]}",
                "cucumber-java:${versions["cucumberJava"]}",
                "com.intellij.properties:${versions["properties"]}",
            )
        )
    }

    testImplementation(project(":plugin-tzatziki"))
    testImplementation(project(":common"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.apache.logging.log4j:log4j-api:2.14.1")
    testImplementation("org.apache.logging.log4j:log4j-core:2.14.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine");
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}

intellijPlatform {
    buildSearchableOptions = false
}
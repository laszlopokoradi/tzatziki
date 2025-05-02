repositories {
    intellijPlatform {
        defaultRepositories()
    }
}

val versions: Map<String, String> by rootProject.extra

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

//intellij {
//    version.set(versions["intellij-version"])
//    plugins.set(listOf(
//        "Gherkin:${versions["gherkin"]}"
//    ))
//}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("${versions["intellij-version"] }")

        plugins(
            listOf(
                "gherkin:${versions["gherkin"]}",
            )
        )
    }

    implementation("io.cucumber:tag-expressions:6.1.2")
}

tasks {
    tasks {
        withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_21.toString()
            targetCompatibility = JavaVersion.VERSION_21.toString()
        }
    }

    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}

intellijPlatform {
    buildSearchableOptions = false
}
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
    }

    implementation("javazoom:jlayer:1.0.1")
    implementation("org.apache.commons:commons-text:1.13.1")
    implementation("org.unbescape:unbescape:1.1.6.RELEASE")
    implementation("org.apache.commons:commons-csv:1.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("commons-codec:commons-codec:1.18.0")
}

tasks {
    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "21"
            targetCompatibility = "21"
        }
    }

    jar {
        archiveBaseName.set(rootProject.name + "-" + project.name)
    }
}

intellijPlatform {
    buildSearchableOptions = false
}
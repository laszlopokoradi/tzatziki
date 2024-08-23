import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

val versions: Map<String, String> by rootProject.extra

dependencies {
    implementation("javazoom:jlayer:1.0.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.unbescape:unbescape:1.1.6.RELEASE")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("commons-codec:commons-codec:1.15")

    intellijPlatform {
        intellijIdeaCommunity("${versions["idea-version"]}")
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false
}

tasks {
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
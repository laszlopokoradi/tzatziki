@file:Suppress("PropertyName")

import java.net.URI

val versions by extra {
    mapOf(
        "intellij-version" to "2025.1",

        "gherkin" to "251.23774.318",        //https://plugins.jetbrains.com/plugin/9164-gherkin/versions
        "properties" to "251.25410.75",     //https://plugins.jetbrains.com/plugin/11594-properties/versions
        "psiViewer" to "223-SNAPSHOT",      //https://plugins.jetbrains.com/plugin/227-psiviewer/versions
        "cucumberJava" to "251.23774.318",   //https://plugins.jetbrains.com/plugin/7212-cucumber-for-java/versions
        "scala" to "2025.1.20",             //https://plugins.jetbrains.com/plugin/1347-scala/versions
    )
}

allprojects {
    group = "io.nimbly.tzatziki"
    version = "18.0.0"

    repositories {
        mavenCentral()
        maven {
            url = URI("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            url = URI("https://dl.bintray.com/jetbrains/intellij-plugin-service")
        }
    }
}
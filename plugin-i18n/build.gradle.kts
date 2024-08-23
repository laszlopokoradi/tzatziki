import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "io.nimbly.translation"
version = "10.2.0"

val notes by extra {"""
       <b>Please kindly report any problem... and Rate &amp; Review this plugin !</b><br/>
       <br/>
       Change notes :
       <ul>
         <!--<li><b>11.0.0</b> Adding support of Deep Translate API</li>-->
         <li><b>10.0.0</b> Adding support of Baidu API</li>
         <li><b>9.0.0</b> Adding support of ChatGPT API</li>
         <li><b>8.0.0</b> Adding support of Microsoft Translator API</li>
         <li><b>7.0.0</b> Adding support of DeepL translation API</li>
         <li><b>6.0.0</b> Apply translation by clicking on hint</li>
         <li><b>5.0.0</b> Smart replacement everywhere using refactoring !</li>
         <li><b>4.0.0</b> Escaping text depending on format : HTML, JSON, CSV, XML, PROPERTIES</li>
         <li><b>3.0.0</b> Commit dialog translation </li>
         <li><b>2.0.0</b> Dictionary definitions</li>
         <li><b>1.2.0</b> Support of camel case</li>
         <li><b>1.1.0</b> Display translation inlined in text</li>
         <li><b>1.0.0</b> Initial version</li>
       </ul>
      """
}

val versions: Map<String, String> by rootProject.extra

dependencies {
    implementation(project(":i18n"))

    intellijPlatform {
        intellijIdeaCommunity("${versions["idea-version"]}")
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false

    pluginConfiguration {
        vendor {
            name.set("Maxime HAMM")
        }
        changeNotes.set(notes)

        ideaVersion {
            sinceBuild.set("242")
        }
    }

    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.2")
        }
    }

    publishing {
        token.set(System.getProperty("PublishToken"))
    }


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

    buildSearchableOptions {
        enabled = false
    }

    jar {
        archiveBaseName.set("translation")
    }
    instrumentedJar {
        // exclude("META-INF/*") // Workaround for runPluginVerifier duplicate plugins...
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

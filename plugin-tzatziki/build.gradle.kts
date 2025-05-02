import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

val versions: Map<String, String> by rootProject.extra
val notes: String =
    """
       <b>Please kindly report any problem... and Rate &amp; Review this plugin !</b><br/>
       <br/>
       Change notes :
       <ul> 
         <li><b>17.0</b> Rewriting breakpoint supports. Now you can set breakpoints from Gherkin ! (Java, Koltin, Scala only) <br/>      
         <li><b>16.6</b> New button to display also files not part of sources/resources path <br/>      
         <li><b>16.5</b> Filter step completion according to tags filtering setup <br/>      
         <li><b>16.3</b> Remove translation stuff : please use 'Translation+' plugin instead !<br/>       
         <li><b>16.2</b> Remove use of deprecated IntelliJ IDEA JDK apis<br/>       
         <li><b>16.1</b> Translate selection using Google Translate (files of any kind, gherkin, java, etc.)<br/>
         <li><b>15.4</b> IntelliJ IDEA 2023.3.2 compatibility</li>
         <li><b>15.3</b> New UI supports</li>
         <li><b>15.2</b> Completion to suggest step parameter types (Java, Kotlin)</li>
         <li><b>15.1</b> Run Cucumber tests from Cucumber+ tool view</li>
         <li><b>15.0</b> Display list of features, let group and filter them by tag names</li>
         <li><b>14.1</b> Kotlin support : generate class and step functions</li>
         <li><b>13.1</b> Java : remove accent from generated method</li>
         <li><b>13.0</b> Java : fix gherkin plugin issue when using '*' as step keyword instead of 'when', 'then', etc.</li>
         <li><b>12.0</b> Upgrade Intellij libs to latest 2022</li>
         <li><b>11.5</b> Allow to fold tables and multi line strings</li>
         <li><b>11.4</b> IntelliJ IDEA 2023.1 compatibility</li>
         <li><b>11.3</b> IntelliJ IDEA 2022.3 compatibility</li>
         <li><b>11.0</b> Tool view to let you select Gherkin tags to filter tests execution, feature exportation to PDF.</li>
         <li><b>10.0</b> Tag completion (cursor location after @)</li>
         <li><b>9.2</b> IntelliJ IDEA 2021.3 compatibility</li>
         <li><b>9.1</b> Export features to PDF now supports Cyrillic. You can also put your prefered font at ".cucumber+/cucumber+.font.ttf" </li>
         <li><b>9.0</b> Line markers indicating the number of uses of step implementations (Java, Kotlin). </li>
         <li><b>8.0</b> Step completion suggests all steps already used in current module, not only steps having an implementation </li>
         <li><b>7.0</b> Breakpoint line markers in Gherkin files (Java, Kotlin) </li>
         <li><b>6.0</b> Run Cucumber test for a single line of a Scenario outline (Java, Kotlin) </li>
         <li><b>5.6</b> IntelliJ IDEA 2021.2 compatibility</li>
         <li><b>5.5</b> Clear tests results annotations using quick fix</li>
         <li><b>5.4</b> Deprecated step inspection for Javascript (and Java, Kotlin)</li>
         <li><b>5.3</b> Deprecated step inspection (instead of annotator... to let it be deactivated if you want)</li>
         <li><b>5.2</b> Navigate from Gerkin step definition to its implementation even after modifing the step text</li>
         <li><b>5.1</b> Deprecated step annotator (Java, Kotlin)</li>
         <li><b>5.0</b> Completion for table header or cells, and markdown images </li>
         <li><b>4.2</b> Tests results annotation is persistent until a scenario is modified </li>
         <li><b>4.1</b> Convert markdown to html while exporting to PDF </li>
         <li><b>4.0</b> Markdown support into header. Including pictures completion and annotation. Pdf ex</li>
         <li><b>3.0</b> Run test then add colors according to tests results</li>
         <li><b>2.4</b> Export PDF landscape or portrait</li>
         <li><b>2.3</b> Ask for review plugin or report bugs and suggestions</li>
         <li><b>2.2</b> More customization for PDF summary</li>
         <li><b>2.1</b> Adding a front page and a summary to the PDF</li>
         <li><b>2.0</b> Exporting feature to PDF</li>
         <li><b>1.5</b> Moving rows and lines uo/down and left/right</li>
         <li><b>1.4</b> Deleting rows and lines improvements</li>
         <li><b>1.3</b> Prevent table structure to be corrupted while using DELETE, BACKSPACE, CUT, etc.</li>
         <li><b>1.2</b> Intellij IDEA 2021.1 EAP Compatibility</li>
         <li><b>1.1</b> Copy from Excel to table</li>
         <li><b>1.0</b> Copy from table to Excel</li>
         <li><b>0.4</b> Add new column by pressing pipe</li>
         <li><b>0.3</b> Add new lines by pressing from line end or from last line</li>
         <li><b>0.2</b> Navigate between cells using tab, backtab and enter</li>
         <li><b>0.1</b> Cucumber table formatting as you go</li>
       </ul>
      """

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
                "org.intellij.intelliLang",
                "JUnit",
            )
        )

        plugins(
            listOf(
                "Gherkin:${versions["gherkin"]}",
                "cucumber-java:${versions["cucumberJava"]}",
                "org.intellij.scala:${versions["scala"]}",
                "com.intellij.properties:${versions["properties"]}",
                "PsiViewer:${versions["psiViewer"]}",
            )
        )
    }

    implementation(project(":common"))
    implementation(project(":i18n"))

    implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-java2d:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-svg-support:1.0.10")

    implementation("org.freemarker:freemarker:2.3.30")
    implementation("com.github.rjeschke:txtmark:0.13")
    implementation("io.cucumber:tag-expressions:4.1.0")

    runtimeOnly(project(":extensions:java-cucumber"))
    runtimeOnly(project(":extensions:kotlin"))
    runtimeOnly(project(":extensions:scala"))
}

configurations.all {
    // This is important for PDF export
    exclude("xml-apis", "xml-apis")
    exclude("xml-apis", "xml-apis-ext")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
//intellij {
//    version.set(versions["intellij-version"])
//
//    plugins.set(listOf(
//        "Gherkin:${versions["gherkin"]}",
//        "Kotlin",
//        "org.intellij.intelliLang",
//        "java",
//        "JUnit",
//        "cucumber-java:${versions["cucumberJava"]}",
//        "org.intellij.scala:${versions["scala"]}",
//        "com.intellij.properties:${versions["properties"]}",
//        "PsiViewer:${versions["psiViewer"]}",
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

    patchPluginXml {
        sinceBuild.set("251")    // 2021.2.4

        changeNotes.set(notes)
    }

    jar {
        archiveBaseName.set(rootProject.name)
    }
    instrumentedJar {
         exclude("META-INF/*") // Workaround for runPluginVerifier duplicate plugins...
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2025.1")
            select {
                sinceBuild = "251"
            }
        }
    }

    publishing  {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

configurations.all {

    resolutionStrategy {

        // Fix for CVE-2020-11987, CVE-2019-17566, CVE-2022-41704, CVE-2022-42890
        force("org.apache.xmlgraphics:batik-parser:1.16")
        force("org.apache.xmlgraphics:batik-anim:1.16")
        force("org.apache.xmlgraphics:batik-awt-util:1.16")
        force("org.apache.xmlgraphics:batik-bridge:1.16")
        force("org.apache.xmlgraphics:batik-codec:1.16")
        force("org.apache.xmlgraphics:batik-constants:1.16")
        force("org.apache.xmlgraphics:batik-css:1.16")
        force("org.apache.xmlgraphics:batik-dom:1.16")
        force("org.apache.xmlgraphics:batik-ext:1.16")
        force("org.apache.xmlgraphics:batik-gvt:1.16")
        force("org.apache.xmlgraphics:batik-parser:1.16")
        force("org.apache.xmlgraphics:batik-script:1.16")
        force("org.apache.xmlgraphics:batik-svg-dom:1.16")
        force("org.apache.xmlgraphics:batik-transcoder:1.16")
        force("org.apache.xmlgraphics:batik-util:1.16")
    }
}
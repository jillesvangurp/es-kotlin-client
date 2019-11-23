import com.avast.gradle.dockercompose.ComposeExtension
import io.inbot.escodegen.EsKotlinCodeGenPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// import com.diffplug.gradle.spotless.SpotlessExtension

// intellij has some bug that causes it to somehow be unable to acknowledge this import exists
// build actually works fine inside and outside intellij
//import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApisExtension

buildscript {
    repositories {
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.github.jillesvangurp:es-kotlin-codegen-plugin:0.9.1-7.4.2")
    }
}


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.60"
    // id("com.diffplug.gradle.spotless") version "3.25.0"
    id("org.jetbrains.dokka") version "0.9.18"
    java


    id("com.avast.gradle.docker-compose") version "0.9.5"
    `maven-publish`
}

apply(plugin = "com.github.jillesvangurp.codegen")

repositories {
    jcenter()
    mavenCentral()
}

sourceSets {
    main {
        kotlin {

        }
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("codegen")
    kotlinOptions.jvmTarget = "1.8"
    this.sourceFilesExtensions

}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    jdkVersion = 8
    outputDirectory = "docs"
    includes = listOf("src/main/kotlin/io/inbot/eskotlinwrapper/module.md")
}

configure<EsKotlinCodeGenPluginExtension> {
    output = projectDir.absolutePath + "/build/generatedcode"
}

sourceSets.main {
    withConvention(KotlinSourceSet::class) {
        kotlin.srcDirs("src/main/kotlin", "build/generatedcode")
    }
}

configure<ComposeExtension> {
    buildAdditionalArgs = listOf("--force-rm")
    forceRecreate = true
}

// configure<SpotlessExtension> {
//     java {
//         removeUnusedImports()
//     }
//     kotlin {
//         ktlint()
//     }

// }

// tasks {
//     "spotlessCheck" {
//         dependsOn("spotlessApply")
//     }
// }

tasks.withType<Test> {
    dependsOn("composeUp")
    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.events = setOf(
        TestLogEvent.FAILED,
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_ERROR,
        TestLogEvent.STANDARD_OUT
    )
}
val artifactName = "es-kotlin-wrapper-client"
val artifactGroup = "com.github.jillesvangurp"

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = artifactGroup
            artifactId = artifactName
            from(components["java"])

        }
    }
    repositories {
        maven {
            name = "myRepo"
            url = uri("file://${buildDir}/repo")
        }
    }
}

val kotlinVersion = "1.3.50"
val elasticVersion = "7.4.2"
val slf4jVersion = "1.7.26"
val junitVersion = "5.5.2"

dependencies {
   api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
   api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
   api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.2")
   api("io.github.microutils:kotlin-logging:1.7.6")

   api("org.apache.commons:commons-lang3:3.9")

   api("com.fasterxml.jackson.core:jackson-annotations:2.10.0")
   api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0")
   api("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")

   api("org.elasticsearch.client:elasticsearch-rest-high-level-client:$elasticVersion")
   api("org.elasticsearch.client:elasticsearch-rest-client-sniffer:$elasticVersion")

    // bring your own logging, but we need some in tests
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.12.1") // es seems to insist on log4j2
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
}

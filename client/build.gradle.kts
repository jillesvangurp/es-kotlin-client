import com.avast.gradle.dockercompose.ComposeExtension
import io.inbot.escodegen.EsKotlinCodeGenPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.resolver.buildSrcSourceRootsFilePath
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources
import javax.xml.transform.Source

// intellij has some bug that causes it to somehow be unable to acknowledge this import exists
// build actually works fine inside and outside intellij
//import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApisExtension

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
//    id("com.diffplug.gradle.spotless") version "3.25.0"
    id("org.jetbrains.dokka") version "0.9.18"
    java

//    id("kotlin")
    id("com.avast.gradle.docker-compose") version "0.9.5"
    `maven-publish`
    // this works because we define a local repo in settings.gradle.kts
    // the flip side is that this breaks any task if the repo does not exist
    // so make sure to build codegen before you do anything
    id("io.inbot.search.codegen") version "1.0"
}

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

//configure<SpotlessExtension> {
//    java {
//        removeUnusedImports()
//    }
//    kotlin {
//        ktlint()
//    }
//
//}

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

val kotlinVersion = "1.3.50"
val elasticVersion = "7.4.0"
val slf4jVersion = "1.7.26"
val junitVersion = "5.5.2"

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.2")
    compile("io.github.microutils:kotlin-logging:1.7.6")

    compile("org.apache.commons:commons-lang3:3.9")

    compile("com.fasterxml.jackson.core:jackson-annotations:2.10.0")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")

    compile("org.elasticsearch.client:elasticsearch-rest-high-level-client:$elasticVersion")
    compile("org.elasticsearch.client:elasticsearch-rest-client-sniffer:$elasticVersion")

    // bring your own logging, but we need some in tests
    testCompile("org.slf4j:slf4j-api:$slf4jVersion")
    testCompile("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    testCompile("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    testCompile("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testCompile("org.apache.logging.log4j:log4j-to-slf4j:2.12.1") // es seems to insist on log4j2
    testCompile("ch.qos.logback:logback-classic:1.2.3")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testCompile("io.mockk:mockk:1.9.3")
    testCompile("com.willowtreeapps.assertk:assertk-jvm:0.20")
}

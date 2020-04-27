import com.avast.gradle.dockercompose.ComposeExtension
import io.inbot.escodegen.EsKotlinCodeGenPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.github.jillesvangurp:es-kotlin-codegen-plugin:1.0-Beta-4-7.6.2")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    // id("com.diffplug.gradle.spotless") version "3.25.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.github.ben-manes.versions") version "0.28.0" // gradle dependencyUpdates -Drevision=release

    java

    id("com.avast.gradle.docker-compose") version "0.10.10"
    `maven-publish`
}

apply(plugin = "com.github.jillesvangurp.codegen")
apply(plugin = "org.jlleitschuh.gradle.ktlint")

repositories {
    jcenter()
    mavenCentral()
}

sourceSets {
    main {
        kotlin {
        }
    }
    // create a new source dir for our examples, ensure the main output is added to the classpath.
    create("examples") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        kotlin {
        }
    }
}

// so we can add dependencies
val examplesImplementation: Configuration by configurations.getting {
    // we can get this because this is generated when we added the examples src dir
    extendsFrom(configurations.implementation.get())
}

// add our production dependencies to the examples
configurations["examplesRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

tasks.withType<KotlinCompile> {
    dependsOn("codegen")
    kotlinOptions.jvmTarget = "1.8"
    this.sourceFilesExtensions
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "docs"
    configuration {
        jdkVersion = 8
        includes = listOf("src/main/kotlin/io/inbot/eskotlinwrapper/module.md")
    }
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

configure<KtlintExtension> {
    ignoreFailures.set(true)
    filter {
        exclude("**/generatedcode/**/*")
    }
}

tasks.withType<Test> {
    dependsOn("examplesClasses", "composeUp")
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
            url = uri("file://$buildDir/repo")
        }
    }
}

val kotlinVersion = "1.3.72"
// match the version used by the es-kotlin-codegen-plugin
val elasticVersion = "7.6.2"
val slf4jVersion = "1.7.26"
val junitVersion = "5.6.0"
val jacksonVersion = "2.11.0"
val ktorVersion = "1.3.2"

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5")
    api("io.github.microutils:kotlin-logging:1.7.9")

    api("org.apache.commons:commons-lang3:3.10")

    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    api("org.elasticsearch.client:elasticsearch-rest-high-level-client:$elasticVersion")
    api("org.elasticsearch.client:elasticsearch-rest-client-sniffer:$elasticVersion")

    // bring your own logging, but we need some in tests
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.13.2") // es seems to insist on log4j2
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")

    examplesImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    examplesImplementation("io.ktor:ktor-server-core:$ktorVersion")
    examplesImplementation("io.ktor:ktor-jackson:$ktorVersion")

    examplesImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    examplesImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    examplesImplementation("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    examplesImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    examplesImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.13.2") // es seems to insist on log4j2
    examplesImplementation("ch.qos.logback:logback-classic:1.2.3")
}

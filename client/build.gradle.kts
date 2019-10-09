// buildscript {
//     ext {
//         kotlinVersion = '1.3.50'
//         elasticsearchVersion = '7.4.0'
//         junitVersion = '5.5.2'
//         jacksonVersion = '2.10.0'
//         slf4jVersion = '1.7.26'
//     }
//     repositories {
//         jcenter()
//     }
//     dependencies {
//         classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
//         classpath "org.jetbrains.dokka:dokka-gradle-plugin:0.9.18"
//         classpath "com.avast.gradle:gradle-docker-compose-plugin:0.9.5"
//         classpath 'com.github.ben-manes:gradle-versions-plugin:0.25.0' // gradle dependencyUpdates -Drevision=release
//         classpath "de.thetaphi:forbiddenapis:2.6"
//         classpath 'com.diffplug.spotless:spotless-plugin-gradle:3.24.3'
//     }
// }

// apply plugin: 'kotlin'
// apply plugin: 'org.jetbrains.dokka'
// apply plugin: 'docker-compose'
// apply plugin: 'project-report' // useful for gradle htmlDependencyReport
// apply plugin: 'com.diffplug.gradle.spotless'
// apply plugin: "de.thetaphi.forbiddenapis"
// apply plugin: 'com.github.ben-manes.versions'

// forbiddenApis {
//     // https://github.com/policeman-tools/forbidden-apis/wiki/GradleUsage
//     bundledSignatures = ["jdk-unsafe-9", "jdk-deprecated-9", "jdk-non-portable", "jdk-internal-9"]
//     // take out "jdk-system-out"
//     signaturesFiles = files("../forbidden_signatures.txt")
//     ignoreFailures = false
// }

// spotless {
//     java {
//         removeUnusedImports() // removes any unused imports
//     }
//     // to fix violations: gradle spotlessApply
//     // in intellij also tweak kotlin imports to require 999 imports before wildcards and remove java.util for always using wildcards
//     // do the same for java imports
//     // wildcards are verboten!
//     // currentlyb breaks offline build due to fucked up transitive dependency on ktlint & kotlin
//     kotlin {
//         // optionally takes a version
//         ktlint()
//     }
// }
// afterEvaluate {
//     // just ffing run it, way to anal to break the build every time
//     tasks.getByName('spotlessCheck').dependsOn(tasks.getByName('spotlessApply'))
// }

// dokka {
//     outputFormat = 'html'
//     jdkVersion = 8
//     outputDirectory = 'docs'
//     includes = ['src/main/kotlin/io/inbot/eskotlinwrapper/module.md']
// }

// tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
//     kotlinOptions {
//         // for whatever reason defaults to 1.6. But since we don't care about android ...
//         jvmTarget = "1.8"
//     }
// }

// sourceSets {
//     main.java.srcDirs += 'src/main/kotlin'
//     test.java.srcDirs += 'src/test/kotlin'
// }

// test {
//     useJUnitPlatform()
//     testLogging {
//         // Make sure output from
//         // standard out or error is shown
//         // in Gradle output.
//         exceptionFormat = 'full'
//         events "passed", "skipped", "failed", "standardOut", "standardError"
//     }
// }

// dockerCompose.isRequiredBy(test)

// dockerCompose {
//     buildAdditionalArgs = ['--force-rm']
//     forceRecreate = true
// }

// compileKotlin {
//     kotlinOptions {
//         freeCompilerArgs = ["-Xjsr305=strict"]
//         jvmTarget = "1.8"
//     }
// }

// tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {

//     kotlinOptions {
//         // for whatever reason defaults to 1.6. But since we don't care about android ...
//         jvmTarget = "1.8"
//         // zero tolerance on warnings
//         allWarningsAsErrors = true
//         freeCompilerArgs += "-Xjsr305=strict"
//         freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
//     }
// }

// compileTestKotlin {
//     kotlinOptions {
//         freeCompilerArgs += "-Xjsr305=strict"
//         freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
//         jvmTarget = "1.8"
//     }
// }

// repositories {
//     maven { url 'https://dl.bintray.com/kotlin/dokka' }
// }
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import com.avast.gradle.dockercompose.ComposeExtension
import com.avast.gradle.dockercompose.tasks.ComposeUp


plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
    id("com.avast.gradle.docker-compose") version "0.9.5"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    jdkVersion = 8
    outputDirectory = "docs"
    includes = listOf("src/main/kotlin/io/inbot/eskotlinwrapper/module.md")
}

configure<ComposeExtension> {
    buildAdditionalArgs = listOf("--force-rm")
    forceRecreate = true 
}

tasks.withType<Test> {
    dependsOn("composeUp")
    useJUnitPlatform()
    // testLogging {
    //     // Make sure output from
    //     // standard out or error is shown
    //     // in Gradle output.
    //     exceptionFormat = 'full'
    //     events "passed", "skipped", "failed", "standardOut", "standardError"
    // }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")
    compile("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.2")
    compile("io.github.microutils:kotlin-logging:1.7.6")

    compile("org.apache.commons:commons-lang3:3.9")

    compile("com.fasterxml.jackson.core:jackson-annotations:2.10.0")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")

    compile("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.4.0")
    compile("org.elasticsearch.client:elasticsearch-rest-client-sniffer:7.4.0")

    // bring your own logging, but we need some in tests
    testCompile("org.slf4j:slf4j-api:1.7.26")
    testCompile("org.slf4j:jcl-over-slf4j:1.7.26")
    testCompile("org.slf4j:log4j-over-slf4j:1.7.26")
    testCompile("org.slf4j:jul-to-slf4j:1.7.26")
    testCompile("org.apache.logging.log4j:log4j-to-slf4j:2.12.1") // es seems to insist on log4j2
    testCompile("ch.qos.logback:logback-classic:1.2.3")

    testCompile("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.5.2")

    testCompile("io.mockk:mockk:1.9.3")
    testCompile("com.willowtreeapps.assertk:assertk:0.20")
}

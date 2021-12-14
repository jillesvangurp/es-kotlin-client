import com.avast.gradle.dockercompose.ComposeExtension
import com.jillesvangurp.escodegen.EsKotlinCodeGenPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.jillesvangurp")
                includeGroup("com.github.jillesvangurp.es-kotlin-codegen-plugin")
            }
        }
    }
    dependencies {
        classpath("com.github.jillesvangurp:es-kotlin-codegen-plugin:_")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("com.github.ben-manes.versions") // gradle dependencyUpdates -Drevision=release
    java

    id("com.avast.gradle.docker-compose")
    `maven-publish`
}

apply(plugin = "com.github.jillesvangurp.codegen")

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io") {
        content {
            includeGroup("com.github.jillesvangurp")
        }
    }
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

tasks.dokkaHtml.configure {
    outputDirectory.set(projectDir.resolve("docs"))
    dokkaSourceSets {
        configureEach {
//            includes.setFrom(files("packages.md", "extra.md","module.md"))
            jdkVersion.set(8)
        }
    }

}

configure<EsKotlinCodeGenPluginExtension> {
    output = projectDir.absolutePath + "/build/generatedcode"
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main/kotlin", "build/generatedcode")

configure<ComposeExtension> {
    buildAdditionalArgs.set(listOf("--force-rm"))
    stopContainers.set(true)
    removeContainers.set(true)
    forceRecreate.set(true)
}

tasks.withType<Test> {
    val isUp = try {
        URL("http://localhost:9999").openConnection().connect()
        true
    } catch (e: kotlin.Exception) {
        false
    }
    if(!isUp) {
        dependsOn(
            "examplesClasses",
            "composeUp"
        )
    }
    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.events = setOf(
        TestLogEvent.FAILED,
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_ERROR,
        TestLogEvent.STANDARD_OUT
    )
    if(!isUp) {
        this.finalizedBy("composeDown")
    }
}

dependencies {
    api(Kotlin.stdlib.jdk8)
    api("org.jetbrains.kotlin:kotlin-reflect:_")
    api(KotlinX.coroutines.jdk8)
    api("io.github.microutils:kotlin-logging:_")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:_")
    implementation("com.fasterxml.jackson.core:jackson-annotations:_")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")

    api("org.elasticsearch.client:elasticsearch-rest-high-level-client:_")
    api("org.elasticsearch.client:elasticsearch-rest-client-sniffer:_")

    // bring your own logging, but we need some in tests
    testImplementation("org.slf4j:slf4j-api:_")
    testImplementation("org.slf4j:jcl-over-slf4j:_")
    testImplementation("org.slf4j:log4j-over-slf4j:_")
    testImplementation("org.slf4j:jul-to-slf4j:_")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:_") // es seems to insist on log4j2
    testImplementation("ch.qos.logback:logback-classic:_")

    testImplementation(Testing.junit.api)
    testImplementation(Testing.junit.engine)

    testImplementation(Testing.mockK)
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
    testImplementation("com.github.jillesvangurp:kotlin4example:_")

    examplesImplementation(Ktor.server.netty)
    examplesImplementation(Ktor.server.core)
    examplesImplementation(Ktor.features.jackson)

    examplesImplementation("org.slf4j:slf4j-api:_")
    examplesImplementation("org.slf4j:jcl-over-slf4j:_")
    examplesImplementation("org.slf4j:log4j-over-slf4j:_")
    examplesImplementation("org.slf4j:jul-to-slf4j:_")
    examplesImplementation("org.apache.logging.log4j:log4j-to-slf4j:_") // es seems to insist on log4j2
    examplesImplementation("ch.qos.logback:logback-classic:_")
}

val artifactName = "es-kotlin-client"
val artifactGroup = "com.github.jillesvangurp"

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar", Jar::class) {
    from(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = artifactGroup
            artifactId = artifactName
            pom {
                description.set("Kotlin client for Elasticsearch that uses the Elastic Java client.")
                name.set(artifactId)
                url.set("https://github.com/jillesvangurp/es-kotlin-client")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/jillesvangurp/es-kotlin-client/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("jillesvangurp")
                        name.set("Jilles van Gurp")
                    }
                }
                scm {
                    url.set("https://github.com/mvysny/karibu-testing")
                }
            }

            from(components["java"])
            artifact(sourceJar)
            artifact(javadocJar)
        }
    }
}

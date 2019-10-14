import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.PublishingExtension
//import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApisExtension


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("com.diffplug.gradle.spotless") version "3.25.0"
    id("org.jetbrains.dokka") version "0.9.18"
    java
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
//    id("de.thetaphi.forbiddenapis") version "2.6"
}

repositories {
    jcenter()
}


// compile bytecode to java 8 (default is java 6)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//configure<CheckForbiddenApisExtension> {
//    // https://github.com/policeman-tools/forbidden-apis/wiki/GradleUsage
//    bundledSignatures = setOf("jdk-unsafe-9", "jdk-deprecated-9", "jdk-non-portable", "jdk-internal-9")
//    // take out "jdk-system-out"
//    signaturesFiles = files("../forbidden_signatures.txt")
//    ignoreFailures = false
//}

dependencies {
    compile("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.4.0")
    compile("org.reflections:reflections:0.9.11")
    implementation("com.squareup:kotlinpoet:1.4.0")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

group = "io.inbot.search"
version = "1.0"

gradlePlugin {
    // Define the plugin
//    val generateExtensionFunctions by plugins.creating {
//        id = "generateExtensionFunctions"
//        group = "io.inbot.search"
//        implementationClass = "io.inbot.escodegen.EsKotlinCodeGenPlugin"
//    }
    plugins {
        create("codegen") {
            id = "io.inbot.search.codegen"
            implementationClass = "io.inbot.escodegen.EsKotlinCodeGenPlugin"
        }
    }
}

configure<PublishingExtension> {
    repositories {
        maven("build/repository")
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

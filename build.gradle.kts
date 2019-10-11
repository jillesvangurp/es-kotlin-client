//plugins {
//    id("org.jetbrains.kotlin.jvm") version "1.3.50"
//    id("com.diffplug.gradle.spotless") version "3.25.0"
//    id("org.jetbrains.dokka") version "0.9.18"
//    java
//}
//
//allprojects {
//  repositories {
//      mavenCentral()
//      maven("https://dl.bintray.com/kotlin/dokka")
//      maven { url = uri("codegen/build/repository") }
//  }
//
//  group = "io.inbot.search"
//  version = "0.0.1-SNAPSHOT"
//}

tasks {

    val codegen by registering(GradleBuild::class) {
        dir = file("codegen")
        tasks = listOf("publish")
    }

    val client by registering(GradleBuild::class) {
        dir = file("client")
        tasks = listOf("build")
    }

    client {
        dependsOn(codegen)
    }
}

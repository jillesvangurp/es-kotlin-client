plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("com.diffplug.gradle.spotless") version "3.25.0"
    id("org.jetbrains.dokka") version "0.9.18"
    java
}

allprojects {
  repositories {
      mavenCentral()
      maven("https://dl.bintray.com/kotlin/dokka")
  }

  group = "io.inbot.search"
  version = "0.0.1-SNAPSHOT"
}


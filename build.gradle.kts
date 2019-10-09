plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    java
}

allprojects {
  repositories {
      mavenCentral()
  }

  group = "io.inbot.search"
  version = "0.0.1-SNAPSHOT"
}


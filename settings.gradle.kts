
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}

refreshVersions {
}

include(":json-dsl")
include(":search-dsls")
rootProject.name = "kt-rest"


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

include(":map-backed-properties")
include(":search-dsls")
rootProject.name = "kt-rest"

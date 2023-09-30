pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")

        // temporary until new version with fork-fix is released
        maven("https://repo.papermc.io/repository/maven-snapshots/") {
            content {
                includeGroupByRegex("io.papermc.paperweight.*")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "CloudPlane"
include("CloudPlane-API", "CloudPlane-Server", "test-plugin")

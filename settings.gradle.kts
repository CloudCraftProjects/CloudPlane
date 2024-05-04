plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "CloudPlane"
include("CloudPlane-API", "CloudPlane-Server", "test-plugin")

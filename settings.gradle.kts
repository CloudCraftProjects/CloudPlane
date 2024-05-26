rootProject.name = "CloudPlane"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("CloudPlane-API", "CloudPlane-Server", "test-plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CloudPlane"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

include(
    "cloudplane-api",
    "test-plugin",
)

// only include setup project if buildscript can be resolved
if (file("cloudplane-server-setup/buildscript").exists()) {
    include("cloudplane-server-setup")
}

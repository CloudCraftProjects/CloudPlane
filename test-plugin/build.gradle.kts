version = "1.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(projects.cloudplaneApi)
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "api_version" to rootProject.providers.gradleProperty("mcVersion").get()
    )

    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}

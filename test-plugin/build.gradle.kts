group = "dev.booky"
version = "1.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(project(":CloudPlane-API"))
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "api_version" to rootProject.providers.gradleProperty("mcVersion").get()
            .split(".", "-").take(2).joinToString(".")
    )

    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}

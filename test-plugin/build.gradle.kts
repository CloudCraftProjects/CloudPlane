group = "dev.booky"
version = "1.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(project(":CloudPlane-API"))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

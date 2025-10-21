import io.papermc.paperweight.patcher.extension.PaperweightPatcherExtension
import io.papermc.paperweight.tasks.RebuildGitPatches
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-library")
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.19"
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
            vendor = JvmVendorSpec.ADOPTIUM
        }
    }

    if (!file(".notest").exists()) {
        dependencies {
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }
    }

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        // if I activate forking (like done in paper + paperweight example),
        // compiling stalls indefinitely on my PC
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isIncremental = true
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io/")
    }
}

configure<PaperweightPatcherExtension> {
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        // paper's example project uses single file patches for buildscripts,
        // but I hate file patches as they are horrible to apply if
        // they fail once; so create a patch dir and use a symbolic
        // link to place the buildscript at the proper location
        listOf("api", "server").forEach { part ->
            val capitalizedPart = part.replaceFirstChar { it.titlecaseChar() }
            patchDir("paper${capitalizedPart}Buildscript") {
                upstreamPath = "paper-$part"
                patchesDir = file("patches/buildscript/$part")
                featurePatchDir = patchesDir.dir(".")
                outputDir = file("cloudplane-$part/buildscript")
                // the relevant part is just the buildscript
                excludes = setOf("src", "patches")
            }
        }
        // api patching
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("patches/api")
            featurePatchDir = patchesDir.dir(".")
            outputDir = file("paper-api")
            excludes = setOf("build.gradle.kts")
        }
    }
}

// see gradle.properties
if (providers.gradleProperty("updatingMinecraft").getOrElse("false").toBoolean()) {
    tasks.withType<RebuildGitPatches> {
        filterPatches = false
    }
}

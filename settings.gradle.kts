pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Loader tooling wrapped by :plugins:loaders. Scoped by group so they
        // are only consulted for their own artifacts.
        maven("https://maven.fabricmc.net/") {
            content { includeGroupByRegex("net\\.fabricmc.*") }
        }
        maven("https://maven.neoforged.net/releases") {
            content { includeGroupByRegex("net\\.neoforged.*") }
        }
        // Stonecutter (multi-version tooling) wrapped by :plugins:multiversion.
        maven("https://maven.kikugie.dev/releases") {
            content { includeGroupByRegex("dev\\.kikugie.*") }
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "modkit"

includeBuild("build-logic")

// Shared libraries
include("libraries:common")
include("libraries:core-api")
include("libraries:plugin-support")

// Plugin modules
include("plugins:core")
include("plugins:loaders")
include("plugins:metadata")
include("plugins:mixins")
include("plugins:publish")
include("plugins:run")
include("plugins:ci")
include("plugins:dependencies")
include("plugins:multiversion")
include("plugins:datagen")
include("plugins:scaffold")

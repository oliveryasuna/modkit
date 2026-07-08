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
include("plugins:publish")

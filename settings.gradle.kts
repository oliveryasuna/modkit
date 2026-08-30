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
    }
}

// TODO: Actually take advantage of this feature.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "modkit"

includeBuild("build-logic")

// Shared libraries
include("libraries:common")
include("libraries:core-api")
include("libraries:plugin-support")
include("libraries:test-util")

// Plugins
include("plugins:core")
include("plugins:loaders")

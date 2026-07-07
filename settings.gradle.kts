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

rootProject.name = "modkit"

includeBuild("build-logic")

// Shared libraries
include("libraries:common")
include("libraries:core-api")

// Plugin modules
include("plugins:core")

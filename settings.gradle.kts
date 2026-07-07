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

// ====================================================================
// Shared libraries (plain Kotlin, published as their own artifacts)
// ====================================================================
include("libraries:common")

// ====================================================================
// Plugin modules
// ====================================================================
include("plugins:core")

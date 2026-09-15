pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

rootProject.name = "modkit-example-dev"

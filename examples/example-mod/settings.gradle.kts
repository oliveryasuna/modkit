import com.oliveryasuna.modkit.core.extension.McLoader

pluginManagement {
    repositories {
        // Modkit is not published to the Plugin Portal yet. Publish it locally
        // first from the repo root:  ./gradlew publishToMavenLocal
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.oliveryasuna.modkit.multiversion.settings") version "0.3.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

// One mod at the root, built for two Minecraft versions × two loaders (4 nodes).
modkitVersions {
    root {
        version("1.21.1", McLoader.FABRIC, McLoader.NEOFORGE)
        version("1.20.6", McLoader.FABRIC, McLoader.NEOFORGE)
    }
}

rootProject.name = "modkit-example"

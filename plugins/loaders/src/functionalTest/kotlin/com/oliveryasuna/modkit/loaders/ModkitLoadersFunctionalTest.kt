package com.oliveryasuna.modkit.loaders

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitLoadersFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun settings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
    }

    private fun buildFile() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("base")
                id("com.oliveryasuna.modkit.loaders")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
            }
            """.trimIndent()
        )
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    @Test
    fun `modkitLoaderInfo runs with no loader set and reports defaults`() {
        settings()
        buildFile()

        // No `modkit.loader` → no base applied → diagnostics-only, stays light.
        val result = runner("modkitLoaderInfo", "-q").build()

        assertTrue(result.output.contains("loader:    <not set>"), result.output)
        assertTrue(result.output.contains("mappings:  MOJMAP"), result.output)
    }

    @Test
    fun `an unknown modkit_loader value fails fast`() {
        settings()
        buildFile()

        // Fails in ActiveLoader.resolve, before any base is applied.
        val result = runner("modkitLoaderInfo", "-Pmodkit.loader=bogus").buildAndFail()

        assertTrue(result.output.contains("Unknown 'modkit.loader' value 'bogus'"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        buildFile()

        // No base → light run; exercises the plugin's own config-cache behavior.
        runner("modkitLoaderInfo", "--configuration-cache").build()
        val second = runner("modkitLoaderInfo", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    // --- Real fixture builds (download Minecraft; slow on first run) ---

    private fun fabricFixtureSettings() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "consumer"
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                }
            }
            """.trimIndent()
        )
    }

    private fun fabricFixtureBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.oliveryasuna.modkit.loaders")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                loaders {
                    fabric { loaderVersion.set("0.19.3") }
                    mappings { parchment.set("2024.11.17") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `builds a remapped Fabric jar with mojmap plus parchment mappings`() {
        fabricFixtureSettings()
        fabricFixtureBuild()

        val result = runner("remapJar", "-Pmodkit.loader=fabric", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":remapJar")?.outcome, result.output)
    }

    private fun neoForgeFixtureSettings() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "consumer"
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.neoforged.net/releases")
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    maven("https://maven.neoforged.net/releases")
                }
            }
            """.trimIndent()
        )
    }

    private fun neoForgeFixtureBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.oliveryasuna.modkit.loaders")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE) }
                loaders {
                    neoforge { version.set("21.1.235") }
                    mappings { parchment.set("2024.11.17") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `builds a NeoForge jar with mojmap plus parchment mappings`() {
        neoForgeFixtureSettings()
        neoForgeFixtureBuild()

        val result = runner("jar", "-Pmodkit.loader=neoforge", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jar")?.outcome, result.output)
    }

}

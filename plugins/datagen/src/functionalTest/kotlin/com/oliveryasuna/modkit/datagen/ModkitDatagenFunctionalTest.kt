package com.oliveryasuna.modkit.datagen

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitDatagenFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    // --- Real Fabric build (downloads Minecraft on first run; slow) ---

    private fun fabricSettings() {
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

    private fun fabricBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            import net.fabricmc.loom.api.LoomGradleExtensionAPI
            import com.oliveryasuna.modkit.plugin.modkitManifestContributions

            plugins {
                // fabric-loom is on datagen's runtime classpath (bundled), so it
                // can be applied by id here without the loaders plugin.
                id("fabric-loom") version "1.17.13"
                id("com.oliveryasuna.modkit.datagen")
            }

            val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)

            dependencies {
                "minecraft"("com.mojang:minecraft:1.21.1")
                "mappings"(loom.officialMojangMappings())
                "modImplementation"("net.fabricmc:fabric-loader:0.16.10")
                "modImplementation"("net.fabricmc.fabric-api:fabric-api:0.115.1+1.21.1")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                datagen {
                    entrypoint.set("com.example.MyDataGen")
                }
            }

            // Reads the shared manifest registry to prove the datagen entrypoint
            // reached it (folded into fabric.mod.json by metadata in a real
            // build). Registry is plugin-internal; access via plugin-support.
            tasks.register("printDatagenEntrypoints") {
                val entrypoints =
                    project.modkitManifestContributions().fabricDatagenEntrypoints
                doLast {
                    println("FABRIC_DATAGEN_ENTRYPOINTS=" + entrypoints.get())
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `configures the Loom runDatagen task without launching the game`() {
        fabricSettings()
        fabricBuild()

        // `help --task` resolves and prints the task if it exists; it never
        // launches the game. Loom registers `runDatagen` from its fabricApi data
        // generation config.
        val result = runner("help", "--task", "runDatagen", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome, result.output)
        assertTrue(result.output.contains("runDatagen"), result.output)
    }

    @Test
    fun `publishes the Fabric datagen entrypoint to the shared manifest registry`() {
        fabricSettings()
        fabricBuild()

        val result = runner("printDatagenEntrypoints", "-q", "--stacktrace").build()

        assertTrue(
            result.output.contains("FABRIC_DATAGEN_ENTRYPOINTS=[com.example.MyDataGen]"),
            result.output
        )
    }

    @Test
    fun `applying datagen and configuring Loom data generation stores a configuration cache entry`() {
        fabricSettings()
        fabricBuild()

        // Loom fingerprints the project directory as a configuration input, so a
        // second invocation in the same sandbox is invalidated by Loom's own
        // directory scan (files Loom wrote during run 1) — a Loom/TestKit trait,
        // not this plugin. A successful STORE proves our wiring is CC-safe; the
        // NeoForge fixture below covers cross-run reuse.
        val result = runner("tasks", "--configuration-cache", "--stacktrace").build()

        assertTrue(result.output.contains("Configuration cache entry stored."), result.output)
    }

    // --- Real NeoForge build (downloads NeoForge on first run; slow) ---

    private fun neoForgeSettings() {
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

    private fun neoForgeBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.neoforged.moddev") version "2.0.141"
                id("com.oliveryasuna.modkit.datagen")
            }

            neoForge {
                version = "21.1.72"
            }

            modkit {
                modId.set("mymod")
                datagen { }
            }

            tasks.register("printResourceSrcDirs") {
                val srcDirs = sourceSets.getByName("main").resources.srcDirs.map { it.path }
                doLast {
                    println("RESOURCE_SRCDIRS=" + srcDirs.joinToString(","))
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `configures the MDG runData task and registers generated as a resources source`() {
        neoForgeSettings()
        neoForgeBuild()

        // runData exists (MDG creates run tasks named run<Name>).
        val help = runner("help", "--task", "runData", "--stacktrace").build()
        assertEquals(TaskOutcome.SUCCESS, help.task(":help")?.outcome, help.output)
        assertTrue(help.output.contains("runData"), help.output)

        // src/main/generated is registered as a main-resources source.
        val srcDirs = runner("printResourceSrcDirs", "-q", "--stacktrace").build()
        assertTrue(
            srcDirs.output.lines().any {
                it.startsWith("RESOURCE_SRCDIRS=") && it.contains("src/main/generated")
            },
            srcDirs.output
        )
    }

    @Test
    fun `configuration cache is stored then reused across NeoForge runs`() {
        neoForgeSettings()
        neoForgeBuild()

        val first = runner("printResourceSrcDirs", "--configuration-cache", "--stacktrace").build()
        assertTrue(first.output.contains("Configuration cache entry stored."), first.output)

        val second = runner("printResourceSrcDirs", "--configuration-cache", "--stacktrace").build()
        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

package com.oliveryasuna.modkit.loaders

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
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
        projectDir.resolve("src/main/resources").mkdirs()
        // Header-only access widener: valid and exercises the wiring without
        // depending on a specific Minecraft member.
        projectDir.resolve("src/main/resources/mymod.accesswidener")
            .writeText("accessWidener v2 named\n")

        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.oliveryasuna.modkit.loaders")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                loaders {
                    fabric {
                        loaderVersion.set("0.19.3")
                        apiVersion.set("0.116.13+1.21.1")
                    }
                    mappings { parchment.set("2024.11.17") }
                    accessWideners.from("src/main/resources/mymod.accesswidener")
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `builds a Fabric jar with parchment, fabric-api, and an access widener`() {
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
        projectDir.resolve("src/main/resources").mkdirs()
        // Widen a stable vanilla class so the generated AT targets a real member.
        projectDir.resolve("src/main/resources/mymod.accesswidener").writeText(
            "accessWidener v2 named\naccessible class net/minecraft/world/entity/Entity\n"
        )

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
                    accessWideners.from("src/main/resources/mymod.accesswidener")
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `builds a NeoForge jar and transpiles the access widener to an AT`() {
        neoForgeFixtureSettings()
        neoForgeFixtureBuild()

        val result = runner("jar", "-Pmodkit.loader=neoforge", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jar")?.outcome, result.output)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateAccessTransformer")?.outcome, result.output)
        val generatedAt = projectDir.resolve("build/modkit/accesstransformer.cfg")
        assertTrue(generatedAt.exists(), "expected generated AT at ${generatedAt.path}")
        assertTrue(
            generatedAt.readText().contains("public net.minecraft.world.entity.Entity"),
            generatedAt.readText()
        )
    }

    // --- Split client (modkit.splitClient property) ---

    private fun writeClientSource() {
        projectDir.resolve("src/client/java/com/example").mkdirs()
        projectDir.resolve("src/client/java/com/example/ClientOnly.java")
            .writeText("package com.example;\npublic class ClientOnly {}\n")
    }

    @Test
    fun `builds a split-client Fabric jar`() {
        fabricFixtureSettings()
        writeClientSource()
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("com.oliveryasuna.modkit.loaders") }
            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                loaders { fabric { loaderVersion.set("0.19.3") } }
            }
            """.trimIndent()
        )

        val result = runner("remapJar", "-Pmodkit.loader=fabric", "-Pmodkit.splitClient=true", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":remapJar")?.outcome, result.output)
        // A `client` source set was created and compiled.
        assertNotNull(result.task(":compileClientJava"), result.output)
    }

    @Test
    fun `builds a split-client NeoForge jar`() {
        neoForgeFixtureSettings()
        writeClientSource()
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("com.oliveryasuna.modkit.loaders") }
            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE) }
                loaders { neoforge { version.set("21.1.235") } }
            }
            """.trimIndent()
        )

        val result = runner("jar", "-Pmodkit.loader=neoforge", "-Pmodkit.splitClient=true", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jar")?.outcome, result.output)
        // A synthesized `client` source set was created and compiled.
        assertNotNull(result.task(":compileClientJava"), result.output)
    }

}

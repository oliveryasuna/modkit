package com.oliveryasuna.modkit.run

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitRunFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    // --- Light checks (no base applied; no Minecraft download) ---

    private fun lightSettings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
    }

    private fun lightBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("base")
                id("com.oliveryasuna.modkit.run")
            }

            modkit {
                modId.set("mymod")
                run {
                    client { jvmArgs.add("-DtestFlag=1") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `plugin applies cleanly and modkitRunInfo reports configs and hotswap status`() {
        lightSettings()
        lightBuild()

        // No base applied → diagnostics-only, stays light (no Minecraft).
        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(result.output.contains("Modkit run configurations:"), result.output)
        assertTrue(result.output.contains("client: enabled=true"), result.output)
        assertTrue(result.output.contains("-DtestFlag=1"), result.output)
        // Hot-swap probe of the running JVM produced guidance.
        assertTrue(result.output.contains("hot-swap", ignoreCase = true), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        lightSettings()
        lightBuild()

        runner("modkitRunInfo", "--configuration-cache").build()
        val second = runner("modkitRunInfo", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    // --- Real Fabric build (downloads Minecraft; slow on first run) ---

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

            plugins {
                // fabric-loom is on run's runtime classpath (bundled), so it can
                // be applied by id here without the loaders plugin.
                id("fabric-loom")
                id("com.oliveryasuna.modkit.run")
            }

            val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)

            dependencies {
                "minecraft"("com.mojang:minecraft:1.21.1")
                "mappings"(loom.officialMojangMappings())
                "modImplementation"("net.fabricmc:fabric-loader:0.19.3")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                run {
                    client { jvmArgs.add("-DtestFlag=1") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `configures a Loom runClient run without launching the game`() {
        fabricSettings()
        fabricBuild()

        // `help --task` resolves and prints the task if it exists; it never
        // launches the game. Loom creates `runClient` from its run container.
        val result = runner("help", "--task", "runClient", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome, result.output)
        assertTrue(result.output.contains("runClient"), result.output)
    }

    @Test
    fun `applying run and configuring Loom runs stores a configuration cache entry`() {
        fabricSettings()
        fabricBuild()

        // Applying run + configuring Loom runs in afterEvaluate must be
        // configuration-cache compatible. `tasks` configures without launching.
        //
        // We assert the entry STORES rather than reuses across two runs: Loom
        // fingerprints the project directory `.` as a configuration input, so a
        // second invocation in the same sandbox is invalidated by Loom's own
        // directory scan (files Loom wrote during run 1) — a Loom/TestKit trait,
        // not this plugin. A successful store proves our afterEvaluate wiring is
        // CC-safe; the light fixture above covers cross-run reuse.
        val result = runner("tasks", "--configuration-cache", "--stacktrace").build()

        assertTrue(result.output.contains("Configuration cache entry stored."), result.output)
    }

    // --- Compat-test variants on a real Loom base ---

    private fun fabricVariantBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            import net.fabricmc.loom.api.LoomGradleExtensionAPI

            plugins {
                id("fabric-loom")
                id("com.oliveryasuna.modkit.run")
            }

            // The staged mod is resolved from a PROJECT repository: Loom adds
            // project repositories, which makes Gradle ignore settings-level
            // repositories, so a variant mod declared only in settings would not
            // resolve. (A plain artifact stands in for a mod jar — the staging
            // mechanism is loader-agnostic.)
            repositories { mavenCentral() }

            val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)

            dependencies {
                "minecraft"("com.mojang:minecraft:1.21.1")
                "mappings"(loom.officialMojangMappings())
                "modImplementation"("net.fabricmc:fabric-loader:0.19.3")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                run {
                    variants {
                        register("compat") {
                            mods("com.google.code.gson:gson:2.11.0")
                            appliesTo("client")
                        }
                    }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `a variant stages its mods and registers a cloned run on Loom`() {
        fabricSettings()
        fabricVariantBuild()

        // The staging task resolves + copies the mod into the variant game dir.
        val sync = runner("syncCompatToRun", "--configuration-cache", "--stacktrace").build()
        assertEquals(TaskOutcome.SUCCESS, sync.task(":syncCompatToRun")?.outcome, sync.output)
        assertTrue(sync.output.contains("Configuration cache entry stored."), sync.output)

        val staged = projectDir.resolve("run/compat/mods/gson-2.11.0.jar")
        assertTrue(staged.exists(), "expected staged mod at ${staged.path}")

        // The cloned run task exists (Loom derives runClientCompat from the run).
        val help = runner("help", "--task", "runClientCompat", "--stacktrace").build()
        assertTrue(help.output.contains("runClientCompat"), help.output)

        // runAllVariants aggregates the variant runs — a dry run shows the graph
        // without launching the game.
        val dryRun = runner("runAllVariants", "--dry-run").build()
        assertTrue(dryRun.output.contains(":runClientCompat "), dryRun.output)
        assertTrue(dryRun.output.contains(":runAllVariants "), dryRun.output)
    }

}

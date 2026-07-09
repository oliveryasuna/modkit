package com.oliveryasuna.modkit.dependencies

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitDependenciesFunctionalTest {

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
                id("com.oliveryasuna.modkit.dependencies")
            }

            modkit {
                modId.set("mymod")
            }

            tasks.register("printRepos") {
                val names = repositories.map { it.name }
                doLast { println("REPOS=" + names) }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `plugin applies cleanly and adds the Modrinth repo by default`() {
        lightSettings()
        lightBuild()

        val result = runner("printRepos", "-q").build()

        assertTrue(result.output.contains("Modrinth"), result.output)
        assertTrue(!result.output.contains("CurseMaven"), result.output)
    }

    @Test
    fun `repository toggles are driven by gradle properties`() {
        lightSettings()
        lightBuild()
        projectDir.resolve("gradle.properties").writeText(
            "modkit.dependencies.modrinth=false\nmodkit.dependencies.curseMaven=true\n"
        )

        val result = runner("printRepos", "-q").build()

        assertTrue(result.output.contains("CurseMaven"), result.output)
        assertTrue(!result.output.contains("Modrinth"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        lightSettings()
        lightBuild()

        // `help` is CC-safe; this proves the plugin's eager repo wiring does not
        // break configuration-cache store/reuse.
        runner("help", "--configuration-cache").build()
        val second = runner("help", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    // --- Real base builds (Loom downloads Minecraft, MDG downloads NeoForge;
    // slow on first run). They confirm the configuration names `mod()`/`nest()`
    // target exist on the real pinned bases. Loom eagerly resolves
    // `modImplementation`, so no unresolvable fixture deps are added here; the
    // mirror mechanism itself is covered by the `routeInto` unit tests.

    private fun loaderSettings() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "consumer"
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases")
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases")
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Loom exposes the modImplementation and include configurations mod and nest target`() {
        loaderSettings()
        projectDir.resolve("build.gradle.kts").writeText(
            """
            import net.fabricmc.loom.api.LoomGradleExtensionAPI

            plugins {
                // dependencies does not bundle Loom, so the consumer resolves it.
                id("fabric-loom") version "1.17.13"
                id("com.oliveryasuna.modkit.dependencies")
            }

            val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)

            dependencies {
                "minecraft"("com.mojang:minecraft:1.21.1")
                "mappings"(loom.officialMojangMappings())
                "modImplementation"("net.fabricmc:fabric-loader:0.16.14")
            }

            tasks.register("checkConfigs") {
                val names = configurations.names
                doLast {
                    require(names.contains("modImplementation")) { "no modImplementation" }
                    require(names.contains("include")) { "no include" }
                    println("LOOM_CONFIGS_OK")
                }
            }
            """.trimIndent()
        )

        val result = runner("checkConfigs", "-q", "--stacktrace").build()

        assertTrue(result.output.contains("LOOM_CONFIGS_OK"), result.output)
    }

    @Test
    fun `ModDevGradle exposes the implementation and jarJar configurations mod and nest target`() {
        loaderSettings()
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("net.neoforged.moddev") version "2.0.141"
                id("com.oliveryasuna.modkit.dependencies")
            }

            neoForge {
                version = "21.1.72"
            }

            tasks.register("checkConfigs") {
                val names = configurations.names
                doLast {
                    require(names.contains("implementation")) { "no implementation" }
                    require(names.contains("jarJar")) { "no jarJar" }
                    println("MDG_CONFIGS_OK")
                }
            }
            """.trimIndent()
        )

        val result = runner("checkConfigs", "-q", "--stacktrace").build()

        assertTrue(result.output.contains("MDG_CONFIGS_OK"), result.output)
    }
}

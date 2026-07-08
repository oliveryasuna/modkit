package com.oliveryasuna.modkit.metadata

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.json.JsonFormat
import com.electronwill.nightconfig.toml.TomlFormat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitMetadataFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun settings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    private fun fabricBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.metadata")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
                metadata {
                    entrypoints { main("com.example.Mod") }
                    dependsOn { required("some_lib", ">=1.0") }
                }
            }
            """.trimIndent()
        )
    }

    private fun neoForgeBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.metadata")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE) }
                metadata {
                    dependsOn { required("some_lib", "[1.0,)") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generates fabric mod json into main resources`() {
        settings()
        fabricBuild()

        runner("processResources", "-Pmodkit.loader=fabric").build()

        val manifest = projectDir.resolve("build/resources/main/fabric.mod.json")
        assertTrue(manifest.exists(), "expected manifest at ${manifest.path}")

        val cfg: Config = JsonFormat.fancyInstance().createParser().parse(manifest.readText())
        assertEquals("mymod", cfg.get<String>("id"))
        assertEquals("1.0.0", cfg.get<String>("version"))
        val entrypoints = cfg.get<Config>("entrypoints")
        assertEquals(listOf("com.example.Mod"), entrypoints.get<List<String>>("main"))
        val depends = cfg.get<Config>("depends")
        assertEquals(">=1.0", depends.get<String>(listOf("some_lib")))
    }

    @Test
    fun `generates neoforge mods toml into main resources`() {
        settings()
        neoForgeBuild()

        runner("processResources", "-Pmodkit.loader=neoforge").build()

        val manifest = projectDir.resolve("build/resources/main/META-INF/neoforge.mods.toml")
        assertTrue(manifest.exists(), "expected manifest at ${manifest.path}")

        val cfg: Config = TomlFormat.instance().createParser().parse(manifest.readText())
        val mods = cfg.get<List<Config>>("mods")
        assertEquals("mymod", mods[0].get<String>("modId"))
        assertEquals("1.0.0", mods[0].get<String>("version"))
        val deps = cfg.get<List<Config>>(listOf("dependencies", "mymod"))
        assertTrue(deps.any { it.get<String>("modId") == "some_lib" }, deps.toString())
    }

    @Test
    fun `validateModMetadata fails on an invalid semver`() {
        settings()
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.metadata")
            }

            modkit {
                modId.set("mymod")
                version.set("not-a-semver")
            }
            """.trimIndent()
        )

        val result = runner("validateModMetadata").buildAndFail()

        assertTrue(result.output.contains("semver"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        fabricBuild()

        runner("processResources", "-Pmodkit.loader=fabric", "--configuration-cache").build()
        val second = runner("processResources", "-Pmodkit.loader=fabric", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

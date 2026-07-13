package com.oliveryasuna.modkit.core

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitCoreFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun settings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
    }

    private fun buildFile(body: String) {
        projectDir.resolve("build.gradle.kts").writeText(body)
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    @Test
    fun `modkitModel prints the resolved model`() {
        settings()
        buildFile(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                version.set("9.9.9")
                minecraft("1.20.4") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC) }
            }
            """.trimIndent()
        )

        val result = runner("modkitModel", "-q").build()

        assertTrue(result.output.contains("modId:     mymod"), result.output)
        assertTrue(result.output.contains("version:   9.9.9"), result.output)
        assertTrue(result.output.contains("toolchain: 17"), result.output)
        assertTrue(result.output.contains("1.20.4 -> [FABRIC]"), result.output)
    }

    @Test
    fun `validateModkitModel fails on an empty target matrix`() {
        settings()
        buildFile(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
            }
            """.trimIndent()
        )

        val result = runner("validateModkitModel").buildAndFail()

        assertTrue(result.output.contains("at least one target is required"), result.output)
    }

    @Test
    fun `check runs validation and reuses the configuration cache`() {
        settings()
        buildFile(
            """
            plugins {
                id("base")
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE) }
            }
            """.trimIndent()
        )

        val first = runner("check", "--configuration-cache").build()
        assertEquals(
            TaskOutcome.SUCCESS,
            first.task(":validateModkitModel")?.outcome,
            first.output
        )

        val second = runner("check", "--configuration-cache").build()
        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    @Test
    fun `modkitDoctor reports the model section and flags missing targets`() {
        settings()
        buildFile(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                version.set("1.2.3")
                // No targets declared → a problem is surfaced.
            }
            """.trimIndent()
        )

        val result = runner("modkitDoctor", "-q", "--configuration-cache").build()

        assertTrue(result.output.contains("[Model]"), result.output)
        assertTrue(result.output.contains("modId:     mymod"), result.output)
        assertTrue(result.output.contains("[Problems]"), result.output)
        assertTrue(result.output.contains("No Minecraft targets declared"), result.output)
    }

}

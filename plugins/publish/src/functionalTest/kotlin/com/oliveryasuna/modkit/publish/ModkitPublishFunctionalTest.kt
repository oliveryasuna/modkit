package com.oliveryasuna.modkit.publish

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitPublishFunctionalTest {

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

    private fun modrinthBuild() {
        // The plain `java` plugin gives a real `jar` task, so artifact-file
        // wiring works without any loader or Minecraft download.
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.publish")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE) }
                publish {
                    modrinth {
                        projectId.set("abc")
                        token.set("dummy")
                    }
                    dryRun.set(true)
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `publishes to modrinth in dry-run without network`() {
        settings()
        modrinthBuild()

        val result = runner("publishMods", "-Pmodkit.loader=neoforge", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":publishModrinth")?.outcome, result.output)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishMods")?.outcome, result.output)
    }

    @Test
    fun `modkitPublish delegates to the upstream aggregate task`() {
        settings()
        modrinthBuild()

        val result = runner("modkitPublish", "-Pmodkit.loader=neoforge", "--dry-run").build()

        assertTrue(result.output.contains(":publishMods"), result.output)
        assertTrue(result.output.contains(":modkitPublish"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        modrinthBuild()

        runner("publishMods", "-Pmodkit.loader=neoforge", "--configuration-cache").build()
        val second = runner("publishMods", "-Pmodkit.loader=neoforge", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

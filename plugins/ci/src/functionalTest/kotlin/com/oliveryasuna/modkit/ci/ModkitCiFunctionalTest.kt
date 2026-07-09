package com.oliveryasuna.modkit.ci

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitCiFunctionalTest {

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

    private fun ciBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.ci")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") {
                    loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.FABRIC)
                    loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE)
                }
            }
            """.trimIndent()
        )
    }

    private val workflow: File
        get() = projectDir.resolve(".github/workflows/ci.yml")

    @Test
    fun `generates the CI workflow with the matrix and pinned actions`() {
        settings()
        ciBuild()

        runner("generateCiWorkflows").build()

        assertTrue(workflow.exists(), "expected workflow at ${workflow.path}")
        val yaml = workflow.readText()
        assertTrue(yaml.contains("name: CI"), yaml)
        assertTrue(yaml.contains("loader: \"fabric\""), yaml)
        assertTrue(yaml.contains("loader: \"neoforge\""), yaml)
        assertTrue(yaml.contains("- uses: actions/checkout@v6"), yaml)
        assertTrue(yaml.contains("- uses: actions/setup-java@v5"), yaml)
        assertTrue(yaml.contains("- uses: gradle/actions/setup-gradle@v6"), yaml)
    }

    @Test
    fun `verifyCiWorkflows fails and reports drift after the file is mutated`() {
        settings()
        ciBuild()

        runner("generateCiWorkflows").build()
        workflow.appendText("\n# drifted\n")

        val result = runner("verifyCiWorkflows").buildAndFail()

        assertTrue(result.output.contains("out of date"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        ciBuild()

        runner("generateCiWorkflows", "--configuration-cache").build()
        val second = runner("generateCiWorkflows", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

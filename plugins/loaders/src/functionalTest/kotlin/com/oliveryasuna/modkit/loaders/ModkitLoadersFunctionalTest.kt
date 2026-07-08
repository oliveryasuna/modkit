package com.oliveryasuna.modkit.loaders

import org.gradle.testkit.runner.GradleRunner
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
    fun `modkitLoaderInfo reports the active loader from the property`() {
        settings()
        buildFile()

        val result = runner("modkitLoaderInfo", "-Pmodkit.loader=neoforge", "-q").build()

        assertTrue(result.output.contains("loader:    NEOFORGE"), result.output)
        assertTrue(result.output.contains("mappings:  mojmap+parchment"), result.output)
    }

    @Test
    fun `an unknown modkit_loader value fails fast`() {
        settings()
        buildFile()

        val result = runner("modkitLoaderInfo", "-Pmodkit.loader=bogus").buildAndFail()

        assertTrue(result.output.contains("Unknown 'modkit.loader' value 'bogus'"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        buildFile()

        runner("modkitLoaderInfo", "-Pmodkit.loader=fabric", "--configuration-cache").build()
        val second = runner("modkitLoaderInfo", "-Pmodkit.loader=fabric", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

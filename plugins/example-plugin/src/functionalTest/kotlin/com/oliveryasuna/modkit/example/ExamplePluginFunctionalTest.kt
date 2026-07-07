package com.oliveryasuna.modkit.example

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ExamplePluginFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun write(name: String, content: String) {
        File(projectDir, name).apply {
            parentFile.mkdirs()
            writeText(content)
        }
    }

    @Test
    fun `greet task prints configured message`() {
        write("settings.gradle.kts", "")
        write(
            "build.gradle.kts",
            """
            plugins {
                id("com.oliveryasuna.modkit.example")
            }

            example {
                message = "Greetings, functional test"
            }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("greet")
            .build()

        assertTrue(result.output.contains("Greetings, functional test"))
    }
}

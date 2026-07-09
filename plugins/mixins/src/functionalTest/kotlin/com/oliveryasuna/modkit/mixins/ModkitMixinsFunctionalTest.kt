package com.oliveryasuna.modkit.mixins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitMixinsFunctionalTest {

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

    private fun writeFile(path: String, content: String) {
        val file = projectDir.resolve(path)
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    private fun build(lintEnabled: Boolean) {
        writeFile(
            "build.gradle.kts",
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.mixins")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                mixins {
                    register("mymod") { pkg.set("com.example") }
                    lint { enabled.set($lintEnabled) }
                }
            }
            """.trimIndent()
        )
    }

    // A local stand-in for the loader-owned annotation. ASM keys off the fully
    // qualified name, so a same-named annotation lets the lint run without
    // Minecraft on the classpath.
    private fun mixinAnnotation() {
        writeFile(
            "src/main/java/org/spongepowered/asm/mixin/Mixin.java",
            """
            package org.spongepowered.asm.mixin;

            public @interface Mixin {
                Class<?>[] value() default {};
                String[] targets() default {};
            }
            """.trimIndent()
        )
    }

    private fun bogusMixin() {
        writeFile(
            "src/main/java/com/example/BogusMixin.java",
            """
            package com.example;

            import org.spongepowered.asm.mixin.Mixin;

            @Mixin(targets = "does/not/Exist")
            public class BogusMixin {
            }
            """.trimIndent()
        )
    }

    @Test
    fun `configures cleanly and lintMixins is a no-op when disabled`() {
        settings()
        build(lintEnabled = false)

        val result = runner("lintMixins").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":lintMixins")?.outcome)
    }

    @Test
    fun `lintMixins fails on a mixin whose target class cannot be resolved`() {
        settings()
        build(lintEnabled = true)
        mixinAnnotation()
        bogusMixin()

        val result = runner("lintMixins").buildAndFail()

        assertTrue(result.output.contains("Mixin lint failed"), result.output)
        assertTrue(result.output.contains("does.not.Exist"), result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        settings()
        build(lintEnabled = false)

        runner("lintMixins", "--configuration-cache").build()
        val second = runner("lintMixins", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

}

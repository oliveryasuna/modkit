package com.oliveryasuna.modkit.testing

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitTestingFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    // For the composite-build test: both modkit plugins come from the
    // included build, so injecting the plugin-under-test classpath too would
    // load core-api twice (two ModkitExtension classes). Resolve everything
    // from the composite instead.
    private fun compositeRunner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args)

    private fun write(path: String, content: String) {
        val file = projectDir.resolve(path)
        file.parentFile.mkdirs()
        file.writeText(content.trimIndent())
    }

    // --- Pure-logic JUnit: no loader, no Minecraft, fast ---

    @Test
    fun `sets up JUnit Platform so pure-logic tests run without any loader`() {
        write("settings.gradle.kts", """rootProject.name = "consumer"""")
        write(
            "build.gradle.kts",
            """
            plugins {
                java
                id("com.oliveryasuna.modkit.testing")
            }

            repositories { mavenCentral() }

            modkit {
                modId.set("mymod")
            }
            """
        )
        write(
            "src/test/java/com/example/LogicTest.java",
            """
            package com.example;

            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertEquals;

            class LogicTest {
                @Test
                void addsUp() {
                    assertEquals(4, 2 + 2);
                }
            }
            """
        )

        val result = runner("test").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":test")?.outcome, result.output)
    }

    @Test
    fun `configuration cache is reused across runs`() {
        write("settings.gradle.kts", """rootProject.name = "consumer"""")
        write(
            "build.gradle.kts",
            """
            plugins {
                java
                id("com.oliveryasuna.modkit.testing")
            }

            repositories { mavenCentral() }

            modkit { modId.set("mymod") }
            """
        )
        write(
            "src/test/java/com/example/LogicTest.java",
            """
            package com.example;
            import org.junit.jupiter.api.Test;
            class LogicTest { @Test void ok() {} }
            """
        )

        runner("test", "--configuration-cache").build()
        val second = runner("test", "--configuration-cache").build()

        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    // --- NeoForge GameTest: real MDG via the loaders plugin (downloads
    // NeoForge; slow on first run). Composed through includeBuild so the loaders
    // plugin applies MDG in its afterEvaluate, matching production timing. ---

    @Test
    fun `gametest enables the NeoForge gameTestServer run`() {
        val repoRoot = System.getProperty("modkit.repoRoot")
                       ?: error("modkit.repoRoot system property not set by the functionalTest task")

        write(
            "settings.gradle.kts",
            """
            includeBuild(${quote(repoRoot)})

            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                }
            }
            rootProject.name = "consumer"
            """
        )
        write("gradle.properties", "modkit.loader=neoforge\n")
        write(
            "build.gradle.kts",
            """
            plugins {
                id("com.oliveryasuna.modkit.loaders")
                id("com.oliveryasuna.modkit.testing")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.21.1") {
                    loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.NEOFORGE)
                }
                loaders {
                    neoforge { version.set("21.1.72") }
                }
                testing { gametest.set(true) }
            }
            """
        )

        // The run is named "gametest", so MDG's task is `runGametest` (task name
        // derives from the run name, not the `gameTestServer` type). `help --task`
        // resolves it if it exists without launching the game.
        val result = compositeRunner("help", "--task", "runGametest", "--stacktrace").build()

        assertTrue(result.output.contains("runGametest"), result.output)
    }

    private fun quote(s: String): String = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

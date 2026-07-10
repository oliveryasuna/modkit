package com.oliveryasuna.modkit.scaffold

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitScaffoldFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun settings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "scaffolder"""")
    }

    private fun scaffoldBuild() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.oliveryasuna.modkit.scaffold")
            }
            """.trimIndent()
        )
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    /** Where scaffold writes when we point it at a sub-directory of the project. */
    private fun out(name: String): File = projectDir.resolve(name)

    @Test
    fun `generates a simple fabric project with the right files and contents`() {
        settings()
        scaffoldBuild()

        runner(
            "modkitInit",
            "-PmodId=mymod",
            "-Pversions=1.21.1",
            "-Ploaders=fabric",
            "-Pmodules=metadata,mixins,run",
            "-PtargetDir=generated",
        ).build()

        val root = out("generated")
        val settings = root.resolve("settings.gradle.kts").readText()
        val build = root.resolve("build.gradle.kts").readText()
        val props = root.resolve("gradle.properties").readText()

        assertTrue(root.resolve("settings.gradle.kts").exists())
        assertTrue(root.resolve("build.gradle.kts").exists())
        assertTrue(root.resolve("gradle.properties").exists())
        assertTrue(root.resolve("src/main/java/com/example/mymod/Mymod.java").exists())
        assertTrue(root.resolve("src/main/resources/mymod.mixins.json").exists())

        assertTrue(
            settings.contains("id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"1.0.0\""),
            settings,
        )
        assertTrue(settings.contains("rootProject.name = \"mymod\""), settings)

        assertTrue(props.contains("modkit.loader=fabric"), props)

        assertTrue(build.contains("id(\"com.oliveryasuna.modkit.loaders\")"), build)
        assertTrue(build.contains("id(\"com.oliveryasuna.modkit.metadata\")"), build)
        assertTrue(build.contains("id(\"com.oliveryasuna.modkit.mixins\")"), build)
        assertTrue(build.contains("id(\"com.oliveryasuna.modkit.run\")"), build)
        assertTrue(build.contains("modkit {"), build)
        assertTrue(build.contains("modId.set(\"mymod\")"), build)
        assertTrue(build.contains("minecraft(\"1.21.1\")"), build)
        assertTrue(build.contains("entrypoints { main(\"com.example.mymod.Mymod\") }"), build)
        assertTrue(build.contains("register(\"mymod\") { pkg.set(\"com.example.mymod.mixin\") }"), build)
    }

    @Test
    fun `generates a multiversion project with the stonecutter layout`() {
        settings()
        scaffoldBuild()

        runner(
            "modkitInit",
            "-PmodId=mymod",
            "-Pversions=1.21.1,1.20.1",
            "-Ploaders=fabric,neoforge",
            "-Pmodules=metadata,run",
            "-PtargetDir=generated",
        ).build()

        val root = out("generated")
        val settings = root.resolve("settings.gradle.kts").readText()
        val build = root.resolve("build.gradle.kts").readText()

        assertTrue(root.resolve("stonecutter.gradle.kts").exists())
        assertFalse(root.resolve("gradle.properties").exists(), "no single-loader gradle.properties in multiversion")

        assertTrue(settings.contains("modkitVersions {"), settings)
        assertTrue(settings.contains("id(\"com.oliveryasuna.modkit.multiversion.settings\")"), settings)
        assertTrue(settings.contains("maven(\"https://maven.kikugie.dev/releases\")"), settings)
        assertTrue(settings.contains("version(\"1.21.1\", McLoader.FABRIC, McLoader.NEOFORGE)"), settings)

        assertTrue(build.contains("id(\"com.oliveryasuna.modkit.multiversion\")"), build)
        assertTrue(build.contains("multiversion {"), build)
        assertFalse(build.contains("modkit.loader="), build)
    }

    @Test
    fun `overwrite is refused without force and allowed with force`() {
        settings()
        scaffoldBuild()

        val args = arrayOf("modkitInit", "-PmodId=mymod", "-Ploaders=fabric", "-PtargetDir=generated")

        runner(*args).build()

        val failed = runner(*args).buildAndFail()
        assertTrue(failed.output.contains("Refusing to overwrite"), failed.output)

        runner(*args, "-Pforce").build()
    }

    @Test
    fun `generated simple fabric project resolves modkit plugins via a composite build`() {
        settings()
        scaffoldBuild()

        runner(
            "modkitInit",
            "-PmodId=mymod",
            "-Pversions=1.21.1",
            "-Ploaders=fabric",
            "-Pmodules=metadata,run",
            "-PtargetDir=generated",
        ).build()

        val repoRoot = System.getProperty("modkit.repoRoot")
                       ?: error("modkit.repoRoot system property not set by the functionalTest task")

        // Inject the composite build so the unpublished Modkit plugins resolve.
        // This is a TEST-ONLY injection; scaffold never emits includeBuild.
        val generated = out("generated")
        val settingsFile = generated.resolve("settings.gradle.kts")
        val original = settingsFile.readText()
        settingsFile.writeText(
            "includeBuild(${quote(repoRoot)})\n\n" + original
        )

        // Keep the JVM small; the generated project downloads Minecraft, so give
        // it room and let Loom fetch what it needs.
        generated.resolve("gradle.properties").appendText("\norg.gradle.jvmargs=-Xmx2G\n")

        val result = GradleRunner.create()
            .withProjectDir(generated)
            .withArguments("build", "--stacktrace")
            .forwardOutput()
            .build()

        assertTrue(
            result.output.contains("BUILD SUCCESSFUL"),
            "expected the generated project to build:\n" + result.output,
        )
    }

    private fun quote(s: String): String =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

package com.oliveryasuna.modkit.publish

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GitChangelogTest {

    @TempDir
    lateinit var projectDir: File

    private fun project(): Project =
        ProjectBuilder.builder().withProjectDir(projectDir).build()

    @Test
    fun `collects commit subjects since the last tag`() {
        assumeTrue(gitAvailable())

        git("init")
        git("config", "user.email", "test@example.com")
        git("config", "user.name", "Test")
        git("commit", "--allow-empty", "-m", "Initial commit")
        git("tag", "v1.0.0")
        git("commit", "--allow-empty", "-m", "Add a shiny feature")

        val project = project()
        val source = project.objects.property(String::class.java).convention("git")
        val changelog = Changelogs.provider(project, source).get()

        assertEquals("Add a shiny feature", changelog)
    }

    @Test
    fun `falls back to the full history when there are no tags`() {
        assumeTrue(gitAvailable())

        git("init")
        git("config", "user.email", "test@example.com")
        git("config", "user.name", "Test")
        git("commit", "--allow-empty", "-m", "Only commit")

        val project = project()
        val source = project.objects.property(String::class.java).convention("git")
        val changelog = Changelogs.provider(project, source).get()

        assertEquals("Only commit", changelog)
    }

    @Test
    fun `resolves to empty outside a git repository`() {
        assumeTrue(gitAvailable())

        val project = project()
        val source = project.objects.property(String::class.java).convention("git")

        assertEquals("", Changelogs.provider(project, source).get())
    }

    private fun gitAvailable(): Boolean =
        try {
            ProcessBuilder("git", "--version").start().waitFor() == 0
        } catch(e: Exception) {
            false
        }

    private fun git(vararg args: String) {
        val process = ProcessBuilder(listOf("git") + args)
            .directory(projectDir)
            .redirectErrorStream(true)
            .start()
        process.inputStream.readBytes()
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed" }
    }

}

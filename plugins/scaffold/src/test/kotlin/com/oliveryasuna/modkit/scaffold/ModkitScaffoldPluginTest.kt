package com.oliveryasuna.modkit.scaffold

import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitScaffoldPluginTest {

    @TempDir
    lateinit var projectDir: File

    private fun project() =
        ProjectBuilder.builder().withProjectDir(projectDir).build().also {
            it.plugins.apply("com.oliveryasuna.modkit.scaffold")
        }

    @Test
    fun `registers the modkitInit task in the modkit group`() {
        val project = project()
        val task = project.tasks.findByName("modkitInit")
        assertNotNull(task)
        assertEquals("modkit", task!!.group)
    }

    @Test
    fun `generate writes a simple project and refuses to overwrite without force`() {
        val project = project()
        val task = project.tasks.named("modkitInit", ModkitInitTask::class.java).get()
        task.modId.set("mymod")
        task.targetDir.set(projectDir)

        task.generate()

        assertTrue(projectDir.resolve("settings.gradle.kts").exists())
        assertTrue(projectDir.resolve("build.gradle.kts").exists())
        assertTrue(projectDir.resolve("gradle.properties").exists())

        // Second run without force must fail.
        val e = assertThrows(org.gradle.api.GradleException::class.java) { task.generate() }
        assertTrue(e.message!!.contains("Refusing to overwrite"), e.message)
    }

    @Test
    fun `force allows overwriting`() {
        val project = project()
        val task = project.tasks.named("modkitInit", ModkitInitTask::class.java).get()
        task.modId.set("mymod")
        task.targetDir.set(projectDir)
        task.generate()

        task.force.set(true)
        task.generate() // must not throw
    }

    @Test
    fun `missing modId fails clearly`() {
        val project = project()
        val task = project.tasks.named("modkitInit", ModkitInitTask::class.java).get()
        task.targetDir.set(projectDir)

        val e = assertThrows(org.gradle.api.GradleException::class.java) { task.generate() }
        assertTrue(e.message!!.contains("modId"), e.message)
    }

    @Test
    fun `default target dir is the project directory`() {
        val project = project()

        @Suppress("UNCHECKED_CAST")
        val provider = project.tasks.named("modkitInit") as TaskProvider<ModkitInitTask>
        val task = provider.get()
        assertEquals(projectDir.canonicalFile, task.targetDir.get().asFile.canonicalFile)
    }
}

package com.oliveryasuna.modkit.ci

import com.oliveryasuna.modkit.ci.extension.CiSpec
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitCiPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitCiPlugin::class.java)
        }

    private val Project.ci: CiSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(CiSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the ci block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("ci"))
    }

    @Test
    fun `provider defaults to github`() {
        assertEquals("github", project().ci.provider.get())
    }

    @Test
    fun `matrixFromTargets defaults to true`() {
        assertTrue(project().ci.matrixFromTargets.get())
    }

    @Test
    fun `java defaults to the modkit toolchain`() {
        val project = project()
        val modkit = project.extensions.getByType(ModkitExtension::class.java)
        assertEquals(modkit.jvm.toolchain.get(), project.ci.java.get())
    }

    @Test
    fun `cache and publishOnTag default to true`() {
        val ci = project().ci
        assertTrue(ci.cache.get())
        assertTrue(ci.publishOnTag.get())
    }

    @Test
    fun `secret names default to the conventional token names`() {
        val secrets = project().ci.secrets
        assertEquals("MODRINTH_TOKEN", secrets.modrinth.get())
        assertEquals("CURSEFORGE_TOKEN", secrets.curseforge.get())
        assertEquals("GITHUB_TOKEN", secrets.github.get())
    }

    @Test
    fun `registers the generate and verify tasks`() {
        val project = project()
        assertNotNull(project.tasks.findByName("generateCiWorkflows"))
        assertNotNull(project.tasks.findByName("verifyCiWorkflows"))
    }

    @Test
    fun `wires verifyCiWorkflows into check`() {
        val project = project()
        project.pluginManager.apply("lifecycle-base")

        val check = project.tasks.getByName("check")
        assertTrue(check.dependsOn.any { dependency ->
            dependency is org.gradle.api.tasks.TaskProvider<*> && dependency.name == "verifyCiWorkflows"
        }, check.dependsOn.toString())
    }

}

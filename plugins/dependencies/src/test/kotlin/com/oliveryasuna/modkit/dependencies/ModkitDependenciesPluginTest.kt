package com.oliveryasuna.modkit.dependencies

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitDependenciesPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitDependenciesPlugin::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `creates non-resolvable mod and nest bucket configurations`() {
        val project = project()
        val mod = project.configurations.findByName(MOD_CONFIGURATION)
        val nest = project.configurations.findByName(NEST_CONFIGURATION)
        assertNotNull(mod)
        assertNotNull(nest)
        assertFalse(mod!!.isCanBeResolved)
        assertFalse(mod.isCanBeConsumed)
        assertFalse(nest!!.isCanBeResolved)
        assertFalse(nest.isCanBeConsumed)
    }

    @Test
    fun `adds the Modrinth repository by default and not CurseMaven`() {
        val names = project().repositories.names
        assertTrue(names.contains("Modrinth"), names.toString())
        assertFalse(names.contains("CurseMaven"), names.toString())
    }

    @Test
    fun `routeInto mirrors dependencies declared before wiring`() {
        val project = project()
        val bucket = project.configurations.getByName(MOD_CONFIGURATION)
        val target = project.configurations.create("someBaseConfig")

        bucket.dependencies.add(project.dependencies.create("com.example:early:1.0"))
        routeInto(project, bucket, "someBaseConfig")

        assertTrue(target.dependencies.any { it.name == "early" })
    }

    @Test
    fun `routeInto mirrors dependencies declared after wiring`() {
        val project = project()
        val bucket = project.configurations.getByName(NEST_CONFIGURATION)
        val target = project.configurations.create("someBaseConfig")

        routeInto(project, bucket, "someBaseConfig")
        bucket.dependencies.add(project.dependencies.create("com.example:late:2.0"))

        assertTrue(target.dependencies.any { it.name == "late" })
    }

    @Test
    fun `routeInto is a no-op when the target configuration is absent`() {
        val project = project()
        val bucket = project.configurations.getByName(MOD_CONFIGURATION)
        // Must not throw when the base never created the target.
        routeInto(project, bucket, "configThatDoesNotExist")
        assertNull(project.configurations.findByName("configThatDoesNotExist"))
    }
}

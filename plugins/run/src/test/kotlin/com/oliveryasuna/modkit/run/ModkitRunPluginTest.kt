package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.run.extension.RunSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitRunPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitRunPlugin::class.java)
        }

    private val Project.run: RunSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(RunSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the run block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("run"))
    }

    @Test
    fun `registers the modkitRunInfo task in the modkit group`() {
        val task = project().tasks.findByName("modkitRunInfo")
        assertNotNull(task)
        assertEquals("modkit", task!!.group)
    }

    @Test
    fun `game directories default per run`() {
        val run = project().run
        assertEquals("run/client", run.client.gameDir.get())
        assertEquals("run/server", run.server.gameDir.get())
        assertEquals("run/data", run.data.gameDir.get())
        assertEquals("run/gametest", run.gametest.gameDir.get())
    }

    @Test
    fun `client and server are enabled by default while data and gametest are not`() {
        val run = project().run
        assertTrue(run.client.enabled.get())
        assertTrue(run.server.enabled.get())
        assertFalse(run.data.enabled.get())
        assertFalse(run.gametest.enabled.get())
    }

    @Test
    fun `auth defaults to false on every run`() {
        val run = project().run
        assertFalse(run.client.auth.get())
        assertFalse(run.server.auth.get())
        assertFalse(run.data.auth.get())
        assertFalse(run.gametest.auth.get())
    }

    @Test
    fun `preferJetBrainsRuntime defaults to true`() {
        assertTrue(project().run.hotswap.preferJetBrainsRuntime.get())
    }

    // --- Compat-test variants ---

    @Test
    fun `variant game directory and enabled default per name`() {
        val run = project().run
        val variant = run.variants.create("modMenu")

        assertEquals("run/modMenu", variant.gameDir.get())
        assertTrue(variant.enabled.get())
    }

    @Test
    fun `extends composes another variant's mods lazily regardless of registration order`() {
        val run = project().run

        // `full` extends `base` before `base` is configured — resolution is lazy.
        val full = run.variants.create("full") { it.extends("base"); it.mods("group:full:1.0") }
        run.variants.create("base") { it.mods("group:base:1.0") }

        val coords = full.modCoordinates.get()
        assertTrue(coords.contains("group:full:1.0"), coords.toString())
        assertTrue(coords.contains("group:base:1.0"), coords.toString())
    }

    @Test
    fun `run kind derives the variant run name and base config`() {
        assertEquals("clientModMenu", RunKind.CLIENT.runName("modMenu"))
        assertEquals("serverFull", RunKind.SERVER.runName("full"))
        assertEquals(RunKind.CLIENT, RunKind.fromName("client"))
        assertNull(RunKind.fromName("bogus"))

        val run = project().run
        assertSame(run.client, run.runByKind(RunKind.CLIENT))
        assertSame(run.gametest, run.runByKind(RunKind.GAMETEST))
    }

}

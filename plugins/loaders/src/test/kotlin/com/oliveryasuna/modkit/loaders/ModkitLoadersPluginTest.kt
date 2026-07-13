package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.diagnostics.ModkitDiagnostics
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitLoadersPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitLoadersPlugin::class.java)
        }

    private val Project.loaders: LoadersSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(LoadersSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the loaders block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("loaders"))
    }

    @Test
    fun `mappings scheme defaults to MOJMAP`() {
        assertEquals(MappingsScheme.MOJMAP, project().loaders.mappings.scheme.get())
    }

    @Test
    fun `exposes fabric, neoforge, and mappings nested specs`() {
        val loaders = project().loaders
        assertNotNull(loaders.fabric)
        assertNotNull(loaders.neoforge)
        assertNotNull(loaders.mappings)
    }

    @Test
    fun `registers the modkitLoaderInfo task`() {
        assertNotNull(project().tasks.findByName("modkitLoaderInfo"))
    }

    @Test
    fun `publishes a Loader diagnostics section and a no-active-loader problem`() {
        val project = project()
        val diagnostics = project.extensions.getByType(ModkitDiagnostics::class.java)

        assertTrue(diagnostics.sections.get().containsKey("Loader"), diagnostics.sections.get().keys.toString())
        // No `modkit.loader` property is set in a ProjectBuilder unit test.
        assertTrue(
            diagnostics.problems.get().any { it.contains("No active loader") },
            diagnostics.problems.get().toString()
        )
    }

}

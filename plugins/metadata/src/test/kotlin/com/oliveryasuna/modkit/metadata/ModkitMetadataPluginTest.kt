package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitMetadataPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitMetadataPlugin::class.java)
        }

    private val Project.metadata: MetadataSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(MetadataSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the metadata block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("metadata"))
    }

    @Test
    fun `environment defaults to any`() {
        assertEquals("*", project().metadata.environment.get())
    }

    @Test
    fun `validation flags default to true`() {
        val validation = project().metadata.validation
        assertTrue(validation.failOnMissingIcon.get())
        assertTrue(validation.failOnInvalidSemver.get())
        assertTrue(validation.failOnUndeclaredMixinConfig.get())
    }

    @Test
    fun `registers validateModMetadata`() {
        assertNotNull(project().tasks.findByName("validateModMetadata"))
    }

    @Test
    fun `no generate task is registered without an active loader`() {
        val project = project()
        assertNull(project.tasks.findByName("generateFabricModJson"))
        assertNull(project.tasks.findByName("generateNeoForgeToml"))
    }

    @Test
    fun `processResources depends on the generate task so the manifest is packaged`() {
        // A provider-based resources.srcDir does not reliably infer this
        // dependency once a loader base rewires the resource pipeline, so the
        // plugin must wire it explicitly — otherwise the jar ships with no
        // manifest and the mod cannot load.
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")
        project.extensions.extraProperties.set("modkit.loader", "fabric")
        project.pluginManager.apply(ModkitMetadataPlugin::class.java)

        val processResources = project.tasks.getByName("processResources")
        val generate = project.tasks.getByName("generateFabricModJson")
        val dependencies = processResources.taskDependencies.getDependencies(processResources)

        assertTrue(
            dependencies.contains(generate),
            "processResources must depend on generateFabricModJson so the manifest is generated before packaging"
        )
    }

}

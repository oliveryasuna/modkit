package com.oliveryasuna.modkit.multiversion

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.multiversion.extension.MultiversionSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitMultiversionPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitMultiversionPlugin::class.java)
        }

    private val Project.multiversion: MultiversionSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(MultiversionSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the multiversion block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("multiversion"))
    }

    @Test
    fun `onVersion is inert outside a Stonecutter build - no node, no throw, no apply`() {
        val project = project()
        // No Stonecutter extension on a plain ProjectBuilder project → runtime
        // stays null → the block must be a safe no-op.
        project.multiversion.onVersion(">=1.21") { it.property("shouldNotApply", "true") }
        assertFalse(project.extensions.extraProperties.has("shouldNotApply"))
    }
}

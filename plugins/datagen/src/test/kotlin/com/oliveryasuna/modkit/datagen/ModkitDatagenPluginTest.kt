package com.oliveryasuna.modkit.datagen

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.datagen.extension.DatagenSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitDatagenPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitDatagenPlugin::class.java)
        }

    private val Project.datagen: DatagenSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(DatagenSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the datagen block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("datagen"))
    }

    @Test
    fun `outputDir defaults to src main generated`() {
        val outputDir = project().datagen.outputDir.get().asFile
        assertTrue(
            outputDir.path.endsWith("src/main/generated"),
            "expected outputDir to end with src/main/generated but was ${outputDir.path}"
        )
    }

    @Test
    fun `includeClient defaults to true`() {
        assertTrue(project().datagen.includeClient.get())
    }

    @Test
    fun `entrypoint is unset by default`() {
        assertFalse(project().datagen.entrypoint.isPresent)
    }

}

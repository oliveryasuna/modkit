package com.oliveryasuna.modkit.mixins

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.mixins.extension.MixinsSpec
import com.oliveryasuna.modkit.plugin.modkitManifestContributions
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitMixinsPluginTest {

    private fun project(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.pluginManager.apply(ModkitMixinsPlugin::class.java)
        }

    private val Project.mixins: MixinsSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(MixinsSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `registers the mixins block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("mixins"))
    }

    @Test
    fun `registers lintMixins`() {
        assertNotNull(project().tasks.findByName("lintMixins"))
    }

    @Test
    fun `register publishes the config file name to the shared registry`() {
        val project = project()
        project.mixins.register("mymod")

        val configs = project.modkitManifestContributions().mixinConfigs.get()
        assertTrue(configs.contains("mymod.mixins.json"), configs.toString())
    }

    @Test
    fun `refmap defaults to the mod id refmap`() {
        val project = project()
        project.extensions.getByType(ModkitExtension::class.java).modId.set("mymod")

        assertEquals("mymod-refmap.json", project.mixins.refmap.get())
    }

    @Test
    fun `environment defaults to any for a registered config`() {
        val project = project()
        project.mixins.register("mymod")

        assertEquals("*", project.mixins.configs.getByName("mymod").environment.get())
    }

    @Test
    fun `lint conventions default to disabled and checking targets`() {
        val lint = project().mixins.lint
        assertFalse(lint.enabled.get())
        assertTrue(lint.checkTargetsExist.get())
    }

}

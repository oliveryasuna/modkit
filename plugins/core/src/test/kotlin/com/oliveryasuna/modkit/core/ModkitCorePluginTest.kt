package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModkitCorePluginTest {

    private fun project(configure: Project.() -> Unit = {}): Project =
        ProjectBuilder.builder().build().also { project ->
            project.configure()
            project.pluginManager.apply(ModkitCorePlugin::class.java)
        }

    private val Project.modkit: ModkitExtension
        get() = extensions.getByType(ModkitExtension::class.java)

    @Test
    fun `registers the modkit extension`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `group and version default to project coordinates`() {
        val project = project {
            group = "com.example"
            version = "1.2.3"
        }
        assertEquals("com.example", project.modkit.group.get())
        assertEquals("1.2.3", project.modkit.version.get())
    }

    @Test
    fun `displayName defaults to modId`() {
        val project = project()
        project.modkit.modId.set("mymod")
        assertEquals("mymod", project.modkit.displayName.get())
    }

    @Test
    fun `layout and authors defaults`() {
        val ext = project().modkit
        assertEquals("main", ext.layout.commonSourceSet.get())
        assertFalse(ext.layout.splitClient.get())
        assertTrue(ext.authors.get().isEmpty())
    }

    @Test
    fun `enabled defaults to true on a lazily added target`() {
        val ext = project().modkit
        ext.minecraft("1.21.1")
        assertTrue(ext.targets.getByName("1.21.1").enabled.get())
    }

    @Test
    fun `toolchain resolves from a single target`() {
        val ext = project().modkit
        ext.minecraft("1.20.4")  // floor: 17
        assertEquals(17, ext.jvm.toolchain.get())
    }

    @Test
    fun `toolchain resolves to the max floor across targets`() {
        val ext = project().modkit
        ext.minecraft("1.20.4") { it.loaders.add(McLoader.FABRIC) }  // 17
        ext.minecraft("1.21.1") { it.loaders.add(McLoader.NEOFORGE) }  // 21
        assertEquals(21, ext.jvm.toolchain.get())
    }

    @Test
    fun `registers the modkitModel and validateModkitModel tasks`() {
        val project = project()
        assertNotNull(project.tasks.findByName("modkitModel"))
        assertNotNull(project.tasks.findByName("validateModkitModel"))
    }

    @Test
    fun `validateModkitModel is wired into check when a lifecycle is present`() {
        val project = project {
            pluginManager.apply("lifecycle-base")
        }
        val checkDeps = project.tasks.getByName("check").taskDependencies
            .getDependencies(project.tasks.getByName("check"))
            .map { it.name }
        assertTrue(checkDeps.contains("validateModkitModel"))
    }

}

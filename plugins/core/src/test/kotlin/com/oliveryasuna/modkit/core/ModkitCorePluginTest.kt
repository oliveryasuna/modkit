package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.diagnostics.ModkitDiagnostics
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

    private val Project.diagnostics: ModkitDiagnostics
        get() = extensions.getByType(ModkitDiagnostics::class.java)

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
    fun `authors defaults to empty`() {
        val ext = project().modkit
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

    // --- modkitDoctor / diagnostics registry ---

    @Test
    fun `registers the modkitDoctor task in the modkit group`() {
        val task = project().tasks.findByName("modkitDoctor")
        assertNotNull(task)
        assertEquals("modkit", task!!.group)
    }

    @Test
    fun `core publishes a Model diagnostics section`() {
        val project = project { group = "com.example"; version = "1.0.0" }
        project.modkit.modId.set("mymod")
        project.modkit.minecraft("1.21.1") { it.loaders.add(McLoader.FABRIC) }

        val sections = project.diagnostics.sections.get()
        assertTrue(sections.containsKey("Model"), sections.keys.toString())
        val model = sections.getValue("Model")
        assertTrue(model.any { it.contains("modId") && it.contains("mymod") }, model.toString())
        assertTrue(model.any { it.contains("1.21.1 -> [FABRIC]") }, model.toString())
    }

    @Test
    fun `an empty target list surfaces a problem`() {
        val withoutTargets = project().diagnostics.problems.get()
        assertTrue(withoutTargets.any { it.contains("No Minecraft targets") }, withoutTargets.toString())

        val project = project()
        project.modkit.minecraft("1.21.1") { it.loaders.add(McLoader.FABRIC) }
        assertTrue(project.diagnostics.problems.get().none { it.contains("No Minecraft targets") })
    }

}

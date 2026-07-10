package com.oliveryasuna.modkit.testing

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.testing.extension.TestingSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test as JupiterTest

class ModkitTestingPluginTest {

    private fun project(applyJava: Boolean = false): Project =
        ProjectBuilder.builder().build().also { project ->
            if(applyJava) project.pluginManager.apply("java")
            project.pluginManager.apply(ModkitTestingPlugin::class.java)
        }

    private val Project.testing: TestingSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(TestingSpec::class.java)
        }

    @JupiterTest
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @JupiterTest
    fun `registers the testing block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("testing"))
    }

    @JupiterTest
    fun `gametest defaults to false`() {
        assertFalse(project().testing.gametest.get())
    }

    @JupiterTest
    fun `applies the JUnit Platform and JUnit 5 dependencies when java is present`() {
        val project = project(applyJava = true)

        val test = project.tasks.getByName("test") as Test
        assertTrue(test.options is JUnitPlatformOptions, "test should use the JUnit Platform")

        val testImplementation = project.configurations.getByName("testImplementation").allDependencies
        assertTrue(testImplementation.any { it.name == "junit-jupiter" }, "junit-jupiter should be added")
        assertTrue(testImplementation.any { it.name == "junit-bom" }, "junit-bom platform should be added")
    }

    @JupiterTest
    fun `does not fail when no java plugin is present`() {
        // No java/java-base → no test task to configure; must not throw.
        val project = project(applyJava = false)
        assertNull(project.tasks.findByName("test"))
    }
}

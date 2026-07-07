package com.oliveryasuna.modkit.example

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ExamplePluginTest {

    @Test
    fun `registers extension and greet task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.oliveryasuna.modkit.example")

        assertNotNull(project.extensions.findByName("example"))
        assertNotNull(project.tasks.findByName("greet"))
    }

    @Test
    fun `extension has default message`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.oliveryasuna.modkit.example")

        val extension = project.extensions.getByType(ExampleExtension::class.java)
        assertEquals("Hello from modkit!", extension.message.get())
    }
}

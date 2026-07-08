package com.oliveryasuna.modkit.publish

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.publish.extension.PublishSpec
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModkitPublishPluginTest {

    @TempDir
    lateinit var projectDir: File

    private fun project(): Project =
        ProjectBuilder.builder().withProjectDir(projectDir).build().also { project ->
            project.pluginManager.apply(ModkitPublishPlugin::class.java)
        }

    private val Project.publish: PublishSpec
        get() {
            val modkit = extensions.getByType(ModkitExtension::class.java)
            return (modkit as ExtensionAware).extensions.getByType(PublishSpec::class.java)
        }

    @Test
    fun `applies core transitively so the modkit extension exists`() {
        assertNotNull(project().extensions.findByName("modkit"))
    }

    @Test
    fun `applies the upstream publish plugin so publishMods exists`() {
        assertNotNull(project().extensions.findByName("publishMods"))
    }

    @Test
    fun `registers the publish block as a child of modkit`() {
        val modkit = project().extensions.getByType(ModkitExtension::class.java)
        assertNotNull((modkit as ExtensionAware).extensions.findByName("publish"))
    }

    @Test
    fun `registers modkitPublish`() {
        assertNotNull(project().tasks.findByName("modkitPublish"))
    }

    @Test
    fun `type and dryRun and changelog source have conventions`() {
        val publish = project().publish
        assertEquals("stable", publish.type.get())
        assertFalse(publish.dryRun.get())
        assertEquals("git", publish.changelog.source.get())
    }

    @Test
    fun `modrinth is disabled without credentials and enabled with them`() {
        val publish = project().publish
        assertFalse(publish.modrinth.enabled.get())

        publish.modrinth.projectId.set("abc")
        publish.modrinth.token.set("secret")
        assertTrue(publish.modrinth.enabled.get())
    }

    @Test
    fun `github requires both repository and token`() {
        val publish = project().publish
        publish.github.repository.set("owner/repo")
        assertFalse(publish.github.enabled.get())

        publish.github.token.set("secret")
        assertTrue(publish.github.enabled.get())
    }

    @Test
    fun `discord is enabled once a webhook is present`() {
        val publish = project().publish
        assertFalse(publish.discord.enabled.get())

        publish.discord.webhook.set("https://discord.example/webhook")
        assertTrue(publish.discord.enabled.get())
    }

    @Test
    fun `changelog resolves a literal source verbatim`() {
        val project = project()
        val source = project.objects.property(String::class.java).convention("Just some notes.")
        assertEquals("Just some notes.", Changelogs.provider(project, source).get())
    }

    @Test
    fun `changelog reads a file source`() {
        val project = project()
        projectDir.resolve("CHANGES.md").writeText("- Fixed a bug.\n")
        val source = project.objects.property(String::class.java).convention("file:CHANGES.md")
        assertEquals("- Fixed a bug.\n", Changelogs.provider(project, source).get())
    }

}

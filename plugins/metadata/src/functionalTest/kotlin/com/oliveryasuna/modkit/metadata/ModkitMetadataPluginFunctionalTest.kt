package com.oliveryasuna.modkit.metadata

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.json.JsonFormat
import com.electronwill.nightconfig.toml.TomlFormat
import com.oliveryasuna.modkit.test.FunctionalTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ModkitMetadataPluginFunctionalTest : FunctionalTestBase() {

    private fun fabricBuild() {
        build(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.metadata")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.ModLoader.FABRIC) }
                metadata {
                    entrypoints { main("com.example.Mod") }
                    dependsOn { required("some_lib", ">=1.0") }
                }
            }
            """.trimIndent()
        )
    }

    private fun neoForgeBuild() {
        build(
            """
            plugins {
                id("java")
                id("com.oliveryasuna.modkit.metadata")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                minecraft("1.21.1") { loaders.add(com.oliveryasuna.modkit.core.extension.ModLoader.NEOFORGE) }
                metadata {
                    dependsOn { required("some_lib", "[1.0,)") }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generates fabric mod json into main resources`() {
        settings()
        fabricBuild()

        runner("processResources", "-Pmodkit.loader=fabric").build()

        val manifest = resolve("build/resources/main/fabric.mod.json")
        assertTrue(manifest.exists(), "expected manifest at ${manifest.path}")

        val cfg: Config = readJson(manifest)
        assertEquals(1, cfg.get("schemaVersion"))
        assertEquals("mymod", cfg.get("id"))
        assertEquals("1.0.0", cfg.get("version"))
        assertEquals("mymod", cfg.get("name"))
        assertEquals("*", cfg.get("environment"))
        val entrypoints = cfg.get<Config>("entrypoints")
        assertEquals(listOf("com.example.Mod"), entrypoints.get("main"))
        val depends = cfg.get<Config>("depends")
        assertEquals("1.21.1", depends.get<String>("minecraft"))
        assertEquals(">=1.0", depends.get<String>("some_lib"))
    }

    @Test
    fun `generates neoforge mods toml into main resources`() {
        settings()
        neoForgeBuild()

        runner("processResources", "-Pmodkit.loader=neoforge").build()

        val manifest = resolve("build/resources/main/META-INF/neoforge.mods.toml")
        assertTrue(manifest.exists(), "expected manifest at ${manifest.path}")

        val cfg: Config = readToml(manifest)
        assertEquals("javafml", cfg.get("modLoader"))
        assertEquals("[1,)", cfg.get("loaderVersion"))
        val mods = cfg.get<List<Config>>("mods")
        assertEquals("mymod", mods[0].get("modId"))
        assertEquals("1.0.0", mods[0].get("version"))
        assertEquals("mymod", mods[0].get("displayName"))
        val deps = cfg.get<List<Config>>(listOf("dependencies", "mymod"))
        assertTrue(deps.any {
            it.get<String>("modId") == "minecraft"
                    && it.get<String>("type") == "required"
                    && it.get<String>("versionRange") == "1.21.1"
        })
        assertTrue(deps.any {
            it.get<String>("modId") == "some_lib"
                    && it.get<String>("type") == "required"
                    && it.get<String>("versionRange") == "[1.0,)"
        })
    }

    // TODO: More tests.

    companion object {

        fun readJson(file: File): Config = JsonFormat.fancyInstance().createParser().parse(file.readText())

        fun readToml(file: File): Config = TomlFormat.instance().createParser().parse(file.readText())

    }

}

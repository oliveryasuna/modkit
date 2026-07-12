package com.oliveryasuna.modkit.metadata

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.json.JsonFormat
import com.electronwill.nightconfig.toml.TomlFormat
import com.oliveryasuna.modkit.metadata.extension.DepConstraint
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ManifestBuildersTest {

    private fun inputs(
        icon: String? = "assets/mymod/icon.png",
        description: String? = "A test mod",
        mixinConfigs: List<String> = emptyList(),
        fabricDatagenEntrypoints: List<String> = emptyList(),
        rawOverrides: Map<String, Any> = emptyMap(),
        minecraftVersion: String? = "1.21.1",
        substituteTokens: Boolean = false
    ): ManifestInputs =
        ManifestInputs(
            modId = "mymod",
            version = "1.0.0",
            group = "com.example",
            displayName = "My Mod",
            description = description,
            authors = listOf("Alice", "Bob"),
            license = "MIT",
            icon = icon,
            homepage = "https://example.com",
            source = "https://example.com/src",
            issues = "https://example.com/issues",
            environment = "*",
            minecraftVersion = minecraftVersion,
            entrypointsMain = listOf("com.example.Mod"),
            entrypointsClient = listOf("com.example.ClientMod"),
            dependencies = linkedMapOf(
                "some_lib" to DepConstraint(">=1.0", DepConstraint.Kind.REQUIRED),
                "opt_lib" to DepConstraint(">=2.0", DepConstraint.Kind.OPTIONAL)
            ),
            mixinConfigs = mixinConfigs,
            fabricDatagenEntrypoints = fabricDatagenEntrypoints,
            rawOverrides = rawOverrides,
            substituteTokens = substituteTokens
        )

    private fun parseJson(text: String): Config =
        JsonFormat.fancyInstance().createParser().parse(text)

    private fun parseToml(text: String): Config =
        TomlFormat.instance().createParser().parse(text)

    @Test
    fun `fabric mod json carries identity, environment, and entrypoints`() {
        val cfg = parseJson(ManifestBuilders.buildFabricModJson(inputs()))

        assertEquals(1, (cfg.get("schemaVersion") as Number).toInt())
        assertEquals("mymod", cfg.get<String>("id"))
        assertEquals("1.0.0", cfg.get<String>("version"))
        assertEquals("My Mod", cfg.get<String>("name"))
        assertEquals("A test mod", cfg.get<String>("description"))
        assertEquals("MIT", cfg.get<String>("license"))
        assertEquals("assets/mymod/icon.png", cfg.get<String>("icon"))
        assertEquals("*", cfg.get<String>("environment"))
        assertEquals(listOf("Alice", "Bob"), cfg.get<List<String>>("authors"))

        val contact = cfg.get<Config>("contact")
        assertEquals("https://example.com", contact.get<String>("homepage"))
        assertEquals("https://example.com/src", contact.get<String>("sources"))
        assertEquals("https://example.com/issues", contact.get<String>("issues"))

        val entrypoints = cfg.get<Config>("entrypoints")
        assertEquals(listOf("com.example.Mod"), entrypoints.get<List<String>>("main"))
        assertEquals(listOf("com.example.ClientMod"), entrypoints.get<List<String>>("client"))
    }

    @Test
    fun `fabric maps required to depends and optional to recommends`() {
        val cfg = parseJson(ManifestBuilders.buildFabricModJson(inputs()))

        val depends = cfg.get<Config>("depends")
        assertEquals("1.21.1", depends.get<String>(listOf("minecraft")))
        assertEquals(">=1.0", depends.get<String>(listOf("some_lib")))
        assertNull(depends.get<String?>(listOf("opt_lib")))

        val recommends = cfg.get<Config>("recommends")
        assertEquals(">=2.0", recommends.get<String>(listOf("opt_lib")))
        assertNull(recommends.get<String?>(listOf("some_lib")))
    }

    @Test
    fun `neoforge toml carries file-level keys and a single mods table`() {
        val cfg = parseToml(ManifestBuilders.buildNeoForgeToml(inputs()))

        assertEquals("javafml", cfg.get<String>("modLoader"))
        assertEquals("[1,)", cfg.get<String>("loaderVersion"))
        assertEquals("MIT", cfg.get<String>("license"))
        assertEquals("https://example.com/issues", cfg.get<String>("issueTrackerURL"))

        val mods = cfg.get<List<Config>>("mods")
        assertEquals(1, mods.size)
        assertEquals("mymod", mods[0].get<String>("modId"))
        assertEquals("1.0.0", mods[0].get<String>("version"))
        assertEquals("My Mod", mods[0].get<String>("displayName"))
        assertEquals("Alice, Bob", mods[0].get<String>("authors"))
        assertEquals("assets/mymod/icon.png", mods[0].get<String>("logoFile"))
    }

    @Test
    fun `neoforge maps constraints to dependency tables with type and range`() {
        val cfg = parseToml(ManifestBuilders.buildNeoForgeToml(inputs()))

        val deps = cfg.get<List<Config>>(listOf("dependencies", "mymod"))
        val byId = deps.associateBy { it.get<String>("modId") }

        assertEquals("required", byId.getValue("minecraft").get<String>("type"))
        assertEquals("1.21.1", byId.getValue("minecraft").get<String>("versionRange"))
        assertEquals("required", byId.getValue("some_lib").get<String>("type"))
        assertEquals(">=1.0", byId.getValue("some_lib").get<String>("versionRange"))
        assertEquals("optional", byId.getValue("opt_lib").get<String>("type"))
        assertEquals(">=2.0", byId.getValue("opt_lib").get<String>("versionRange"))
    }

    @Test
    fun `raw overrides win over generated keys`() {
        val cfg = parseToml(
            ManifestBuilders.buildNeoForgeToml(
                inputs(rawOverrides = mapOf("loaderVersion" to "[2,)", "custom" to "value"))
            )
        )

        assertEquals("[2,)", cfg.get<String>("loaderVersion"))
        assertEquals("value", cfg.get<String>("custom"))
    }

    @Test
    fun `fabric lists mixin configs when present and omits them when empty`() {
        val withMixins = parseJson(
            ManifestBuilders.buildFabricModJson(
                inputs(mixinConfigs = listOf("mymod.mixins.json", "mymod-client.mixins.json"))
            )
        )
        assertEquals(
            listOf("mymod.mixins.json", "mymod-client.mixins.json"),
            withMixins.get<List<String>>("mixins")
        )

        val withoutMixins = parseJson(ManifestBuilders.buildFabricModJson(inputs()))
        assertFalse(withoutMixins.contains("mixins"))
    }

    @Test
    fun `fabric folds datagen entrypoints into the fabric-datagen entrypoint list`() {
        val withDatagen = parseJson(
            ManifestBuilders.buildFabricModJson(
                inputs(fabricDatagenEntrypoints = listOf("com.example.MyDataGenerator"))
            )
        )
        val entrypoints = withDatagen.get<Config>("entrypoints")
        assertEquals(listOf("com.example.MyDataGenerator"), entrypoints.get<List<String>>("fabric-datagen"))

        // Absent when nothing publishes an entrypoint.
        val withoutDatagen = parseJson(ManifestBuilders.buildFabricModJson(inputs()))
        assertFalse(withoutDatagen.get<Config>("entrypoints").contains("fabric-datagen"))
    }

    @Test
    fun `neoforge emits a mixins table per config when present and omits when empty`() {
        val withMixins = parseToml(
            ManifestBuilders.buildNeoForgeToml(
                inputs(mixinConfigs = listOf("mymod.mixins.json", "mymod-client.mixins.json"))
            )
        )
        val mixins = withMixins.get<List<Config>>("mixins")
        assertEquals(
            listOf("mymod.mixins.json", "mymod-client.mixins.json"),
            mixins.map { it.get<String>("config") }
        )

        val withoutMixins = parseToml(ManifestBuilders.buildNeoForgeToml(inputs()))
        assertFalse(withoutMixins.contains("mixins"))
    }

    @Test
    fun `an unset icon is omitted from fabric output`() {
        val cfg = parseJson(ManifestBuilders.buildFabricModJson(inputs(icon = null)))

        assertFalse(cfg.contains("icon"))
    }

    @Test
    fun `output is deterministic for identical input`() {
        assertEquals(
            ManifestBuilders.buildFabricModJson(inputs()),
            ManifestBuilders.buildFabricModJson(inputs())
        )
        assertEquals(
            ManifestBuilders.buildNeoForgeToml(inputs()),
            ManifestBuilders.buildNeoForgeToml(inputs())
        )
    }

    @Test
    fun `both loaders emit the mod id and version`() {
        assertTrue(ManifestBuilders.buildFabricModJson(inputs()).contains("mymod"))
        assertTrue(ManifestBuilders.buildNeoForgeToml(inputs()).contains("mymod"))
    }

    // --- Token substitution (opt-in) ---

    @Test
    fun `tokens are left literal when substitution is off`() {
        val cfg = parseJson(
            ManifestBuilders.buildFabricModJson(
                inputs(description = "Build \${version}", substituteTokens = false)
            )
        )
        assertEquals("Build \${version}", cfg.get<String>("description"))
    }

    @Test
    fun `substitution expands all five tokens in string values including raw overrides`() {
        val cfg = parseJson(
            ManifestBuilders.buildFabricModJson(
                inputs(
                    description = "\${name} \${version} for \${minecraft}",
                    rawOverrides = mapOf("custom" to mapOf("built" to "\${modId}@\${group}")),
                    substituteTokens = true
                )
            )
        )
        assertEquals("My Mod 1.0.0 for 1.21.1", cfg.get<String>("description"))
        assertEquals("mymod@com.example", cfg.get<String>(listOf("custom", "built")))
    }

    @Test
    fun `substitution expands tokens in the neoforge toml too`() {
        val toml = ManifestBuilders.buildNeoForgeToml(
            inputs(description = "v\${version}", substituteTokens = true)
        )
        assertTrue(toml.contains("v1.0.0"), toml)
        assertFalse(toml.contains("\${version}"), toml)
    }

    @Test
    fun `an unknown token fails fast`() {
        val ex = assertThrows(GradleException::class.java) {
            ManifestBuilders.buildFabricModJson(
                inputs(description = "\${bogus}", substituteTokens = true)
            )
        }
        assertTrue(ex.message!!.contains("Unknown token '\${bogus}'"), ex.message)
    }

    @Test
    fun `a token whose value is unavailable fails fast`() {
        val ex = assertThrows(GradleException::class.java) {
            ManifestBuilders.buildFabricModJson(
                inputs(description = "\${minecraft}", minecraftVersion = null, substituteTokens = true)
            )
        }
        assertTrue(ex.message!!.contains("unavailable"), ex.message)
    }

}

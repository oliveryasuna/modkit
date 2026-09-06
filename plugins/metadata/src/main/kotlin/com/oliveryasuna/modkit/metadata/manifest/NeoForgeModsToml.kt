package com.oliveryasuna.modkit.metadata.manifest

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.toml.TomlFormat
import com.oliveryasuna.modkit.metadata.extension.DepConstraint
import com.oliveryasuna.modkit.metadata.manifest.NeoForgeModsToml.mixins

/**
 * Builds `neoforge.mods.toml` from a [ManifestInputs].
 *
 * The single `[[mods]]` entry carries the identity; mixins and dependencies are
 * arrays of tables. Raw overrides go on last, then optional token expansion.
 */
internal object NeoForgeModsToml {

    fun build(inputs: ManifestInputs): String {
        val format = TomlFormat.instance()
        val root = orderedConfig(format)

        root.set<Any?>("modLoader", "javafml")
        root.set<Any?>("loaderVersion", "[1,)")
        inputs.license?.let { root.set<Any?>("license", it) }
        inputs.issues?.let { root.set<Any?>("issueTrackerURL", it) }

        root.set<Any?>("mods", arrayListOf(mod(format, inputs)))

        mixins(format, inputs)?.let { root.set<Any?>(listOf("mixins"), it) }
        dependencies(format, inputs)?.let { root.set<Any?>("dependencies", it) }

        root.mergeRaw(format, inputs.rawOverrides)
        if(inputs.substituteTokens) TokenSubstitution.apply(root, TokenSubstitution.tokensFrom(inputs))

        return format.createWriter().writeToString(root)
    }

    /** The single `[[mods]]` table. */
    private fun mod(format: ConfigFormat<out Config>, inputs: ManifestInputs): Config {
        val mod = orderedConfig(format)
        mod.set<Any?>("modId", inputs.modId)
        mod.set<Any?>("version", inputs.version)
        mod.set<Any?>("displayName", inputs.displayName)
        inputs.description?.let { mod.set<Any?>("description", it) }
        if(inputs.authors.isNotEmpty()) mod.set<Any?>("authors", inputs.authors.joinToString(", "))
        inputs.icon?.let { mod.set<Any?>("logoFile", it) }
        return mod
    }

    /** One `[[mixins]]` table per config, or `null` when there are none. */
    private fun mixins(format: ConfigFormat<out Config>, inputs: ManifestInputs): List<Config>? {
        if(inputs.mixinConfigs.isEmpty()) return null
        return inputs.mixinConfigs.map { config ->
            orderedConfig(format).also { it.set<Any?>("config", config) }
        }
    }

    /**
     * The `[[dependencies.<modId>]]` array (Minecraft first, then declared
     * deps), or `null` when empty. The mod id is a single-element path so a
     * dotted id stays one key.
     */
    private fun dependencies(format: ConfigFormat<out Config>, inputs: ManifestInputs): Config? {
        val entries = ArrayList<Config>()
        inputs.minecraftVersion?.let { entries.add(dependency(format, "minecraft", "required", it)) }
        for((id, constraint) in inputs.dependencies) {
            val type = if(constraint.kind == DepConstraint.Kind.REQUIRED) "required" else "optional"
            entries.add(dependency(format, id, type, constraint.range))
        }
        if(entries.isEmpty()) return null

        return orderedConfig(format).also { it.set<Any?>(listOf(inputs.modId), entries) }
    }

    private fun dependency(
        format: ConfigFormat<out Config>,
        modId: String,
        type: String,
        versionRange: String
    ): Config {
        val entry = orderedConfig(format)
        entry.set<Any?>("modId", modId)
        entry.set<Any?>("type", type)
        entry.set<Any?>("versionRange", versionRange)
        return entry
    }

}

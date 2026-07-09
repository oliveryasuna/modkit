package com.oliveryasuna.modkit.metadata

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.json.JsonFormat
import com.electronwill.nightconfig.toml.TomlFormat
import com.oliveryasuna.modkit.metadata.extension.DepConstraint

/**
 * Turns a resolved [ManifestInputs] into a serialized loader manifest. Every
 * config is backed by a [LinkedHashMap] so key order follows insertion order,
 * making the output deterministic across runs. User
 * [ManifestInputs.rawOverrides] are merged last so they win over generated
 * values.
 */
internal object ManifestBuilders {

    fun buildFabricModJson(inputs: ManifestInputs): String {
        val format = JsonFormat.fancyInstance()
        val root = orderedConfig(format)

        root.set<Any?>("schemaVersion", 1)
        root.set<Any?>("id", inputs.modId)
        root.set<Any?>("version", inputs.version)
        root.set<Any?>("name", inputs.displayName)
        inputs.description?.let { root.set<Any?>("description", it) }
        if(inputs.authors.isNotEmpty()) root.set<Any?>("authors", ArrayList(inputs.authors))
        inputs.license?.let { root.set<Any?>("license", it) }
        inputs.icon?.let { root.set<Any?>("icon", it) }
        root.set<Any?>("environment", inputs.environment)

        val contact = orderedConfig(format)
        inputs.homepage?.let { contact.set<Any?>("homepage", it) }
        inputs.source?.let { contact.set<Any?>("sources", it) }
        inputs.issues?.let { contact.set<Any?>("issues", it) }
        if(!contact.isEmpty) root.set<Any?>("contact", contact)

        val entrypoints = orderedConfig(format)
        if(inputs.entrypointsMain.isNotEmpty()) entrypoints.set<Any?>("main", ArrayList(inputs.entrypointsMain))
        if(inputs.entrypointsClient.isNotEmpty()) entrypoints.set<Any?>("client", ArrayList(inputs.entrypointsClient))
        if(inputs.fabricDatagenEntrypoints.isNotEmpty()) entrypoints.set<Any?>("fabric-datagen", ArrayList(inputs.fabricDatagenEntrypoints))
        if(!entrypoints.isEmpty) root.set<Any?>("entrypoints", entrypoints)

        if(inputs.mixinConfigs.isNotEmpty()) root.set<Any?>("mixins", ArrayList(inputs.mixinConfigs))

        val depends = orderedConfig(format)
        inputs.minecraftVersion?.let { depends.set<Any?>(listOf("minecraft"), it) }
        val recommends = orderedConfig(format)
        for((id, constraint) in inputs.dependencies) {
            when(constraint.kind) {
                DepConstraint.Kind.REQUIRED -> depends.set<Any?>(listOf(id), constraint.range)
                DepConstraint.Kind.OPTIONAL -> recommends.set<Any?>(listOf(id), constraint.range)
            }
        }
        if(!depends.isEmpty) root.set<Any?>("depends", depends)
        if(!recommends.isEmpty) root.set<Any?>("recommends", recommends)

        mergeRaw(format, root, inputs.rawOverrides)

        return format.createWriter().writeToString(root)
    }

    fun buildNeoForgeToml(inputs: ManifestInputs): String {
        val format = TomlFormat.instance()
        val root = orderedConfig(format)

        root.set<Any?>("modLoader", "javafml")
        root.set<Any?>("loaderVersion", "[1,)")
        inputs.license?.let { root.set<Any?>("license", it) }
        inputs.issues?.let { root.set<Any?>("issueTrackerURL", it) }

        val mod = orderedConfig(format)
        mod.set<Any?>("modId", inputs.modId)
        mod.set<Any?>("version", inputs.version)
        mod.set<Any?>("displayName", inputs.displayName)
        inputs.description?.let { mod.set<Any?>("description", it) }
        if(inputs.authors.isNotEmpty()) mod.set<Any?>("authors", inputs.authors.joinToString(", "))
        inputs.icon?.let { mod.set<Any?>("logoFile", it) }
        root.set<Any?>("mods", arrayListOf(mod))

        if(inputs.mixinConfigs.isNotEmpty()) {
            val mixinEntries = ArrayList<Config>()
            for(config in inputs.mixinConfigs) {
                val entry = orderedConfig(format)
                entry.set<Any?>("config", config)
                mixinEntries.add(entry)
            }
            root.set<Any?>(listOf("mixins"), mixinEntries)
        }

        val entries = ArrayList<Config>()
        inputs.minecraftVersion?.let { entries.add(dependencyEntry(format, "minecraft", "required", it)) }
        for((id, constraint) in inputs.dependencies) {
            val type = if(constraint.kind == DepConstraint.Kind.REQUIRED) "required" else "optional"
            entries.add(dependencyEntry(format, id, type, constraint.range))
        }
        if(entries.isNotEmpty()) {
            val dependencies = orderedConfig(format)
            dependencies.set<Any?>(listOf(inputs.modId), entries)
            root.set<Any?>("dependencies", dependencies)
        }

        mergeRaw(format, root, inputs.rawOverrides)

        return format.createWriter().writeToString(root)
    }

    private fun dependencyEntry(
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

    private fun mergeRaw(
        format: ConfigFormat<out Config>,
        root: Config,
        raw: Map<String, Any>
    ) {
        for((key, value) in raw) {
            root.set<Any?>(listOf(key), toConfigValue(format, value))
        }
    }

    private fun toConfigValue(format: ConfigFormat<out Config>, value: Any): Any =
        when(value) {
            is Map<*, *> -> {
                val nested = orderedConfig(format)
                for((k, v) in value) {
                    if(k != null && v != null) nested.set<Any?>(listOf(k.toString()), toConfigValue(format, v))
                }
                nested
            }

            is List<*> -> value.mapNotNull { element -> element?.let { toConfigValue(format, it) } }
            else -> value
        }

    private fun orderedConfig(format: ConfigFormat<out Config>): Config =
        format.createConfig { LinkedHashMap<String, Any>() }

}

package com.oliveryasuna.modkit.metadata.manifest

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.json.JsonFormat
import com.oliveryasuna.modkit.metadata.extension.DepConstraint

/** Builds `fabric.mod.json` from a [ManifestInputs]. */
internal object FabricModJson {

    fun build(inputs: ManifestInputs): String {
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

        contact(format, inputs)?.let { root.set<Any?>("contact", it) }
        entrypoints(format, inputs)?.let { root.set<Any?>("entrypoints", it) }

        if(inputs.mixinConfigs.isNotEmpty()) root.set<Any?>("mixins", ArrayList(inputs.mixinConfigs))

        writeDependencies(format, root, inputs)

        root.mergeRaw(format, inputs.rawOverrides)
        if(inputs.substituteTokens) TokenSubstitution.apply(root, TokenSubstitution.tokensFrom(inputs))

        return format.createWriter().writeToString(root)
    }

    /** The `contact` object, or `null` when no URL was set. */
    private fun contact(
        format: ConfigFormat<out Config>,
        inputs: ManifestInputs
    ): Config? {
        val contact = orderedConfig(format)
        inputs.homepage?.let { contact.set<Any?>("homepage", it) }
        inputs.source?.let { contact.set<Any?>("sources", it) }
        inputs.issues?.let { contact.set<Any?>("issues", it) }
        return contact.takeUnless { it.isEmpty }
    }

    /** The `entrypoints` object, or `null` when there are none. */
    private fun entrypoints(
        format: ConfigFormat<out Config>,
        inputs: ManifestInputs
    ): Config? {
        val entrypoints = orderedConfig(format)
        if(inputs.entrypointsMain.isNotEmpty()) entrypoints.set<Any?>("main", ArrayList(inputs.entrypointsMain))
        if(inputs.entrypointsClient.isNotEmpty()) entrypoints.set<Any?>("client", ArrayList(inputs.entrypointsClient))
        if(inputs.fabricDatagenEntrypoints.isNotEmpty()) {
            entrypoints.set<Any?>("fabric-datagen", ArrayList(inputs.fabricDatagenEntrypoints))
        }
        return entrypoints.takeUnless { it.isEmpty }
    }

    /**
     * Fills `depends` (Minecraft plus required deps) and `recommends` (optional
     * deps). Dependency ids are set as single-element paths so a dotted id
     * stays one key.
     */
    private fun writeDependencies(
        format: ConfigFormat<out Config>,
        root: Config, inputs: ManifestInputs
    ) {
        val depends = orderedConfig(format)
        val recommends = orderedConfig(format)

        inputs.minecraftVersion?.let { depends.set<Any?>(listOf("minecraft"), it) }
        for((id, constraint) in inputs.dependencies) {
            val bucket = if(constraint.kind == DepConstraint.Kind.REQUIRED) depends else recommends
            bucket.set<Any?>(listOf(id), constraint.range)
        }

        if(!depends.isEmpty) root.set<Any?>("depends", depends)
        if(!recommends.isEmpty) root.set<Any?>("recommends", recommends)
    }

}

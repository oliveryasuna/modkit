package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunVariant

/**
 * The variant-iteration both backends share.
 *
 * Every enabled variant stages its mods once, then for each valid run kind it
 * names, [createRun] realizes the actual run (the only loader-specific part) and
 * this wires the staging dependency and the aggregate task around it. An unknown
 * kind is warned and skipped, never fatal.
 */
internal fun RunContext.forEachVariantRun(
    createRun: (
        variant: RunVariant,
        kind: RunKind,
        runName: String
    ) -> Unit
) {
    run.variants.forEach { variant ->
        if(!variant.enabled.getOrElse(true)) return@forEach

        val syncTask = stageVariantMods(project, variant)

        variant.appliesToRuns.getOrElse(emptySet()).forEach kinds@{ kindName ->
            val kind = RunKind.fromName(kindName)
            if(kind == null) {
                project.logger.warn("modkit.run: variant '${variant.name}' applies to unknown run kind '$kindName'; skipping.")
                return@kinds
            }

            val runName = kind.runName(variant.name)
            createRun(variant, kind, runName)
            wireSyncDependency(project, runName, syncTask)
            aggregateVariantRun(project, runName)
        }
    }
}

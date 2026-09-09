package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.plugin.modkitDiagnostics
import com.oliveryasuna.modkit.run.extension.RunSpec

/**
 * Publishes the enabled runs and any variants as a "Runs" section in the
 * `modkitDoctor` report.
 */
internal object RunDiagnostics : PluginFeature<RunContext> {

    private const val SECTION_TITLE: String = "Runs"

    override fun install(ctx: RunContext) {
        ctx.project.modkitDiagnostics().sections.put(
            SECTION_TITLE,
            ctx.project.provider { report(ctx.run) },
        )
    }

    private fun report(run: RunSpec): List<String> = buildList {
        fixedRuns(run).forEach { (name, config) ->
            if(config.enabled.getOrElse(false)) add("$name: gameDir=${config.gameDir.orNull}")
        }

        if(run.variants.isNotEmpty()) {
            add("variants:")
            run.variants.forEach { variant ->
                val kinds = variant.appliesToRuns.getOrElse(emptySet())
                val mods = variant.modCoordinates.getOrElse(emptyList()).size
                add("  - ${variant.name} -> $kinds ($mods mods)")
            }
        }
    }
}

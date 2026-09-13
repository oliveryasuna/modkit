package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.plugin.modkitDiagnostics
import com.oliveryasuna.modkit.run.extension.RunSpec
import org.gradle.api.file.Directory

/**
 * Publishes the enabled runs and any variants as a "Runs" section in the
 * `modkitDoctor` report.
 */
internal object RunDiagnostics : PluginFeature<RunContext> {

    private const val SECTION_TITLE: String = "Runs"

    override fun install(ctx: RunContext) {
        ctx.project.modkitDiagnostics().sections.put(
            SECTION_TITLE,
            ctx.project.provider { report(ctx.run, ctx.project.layout.projectDirectory) },
        )
    }

    private fun report(
        run: RunSpec,
        projectDir: Directory
    ): List<String> = buildList {
        fixedRuns(run).forEach { (name, config) ->
            if(config.enabled.getOrElse(false)) {
                val gameDir = config.gameDir.orNull?.relativeToProject(projectDir)
                add("$name: gameDir=$gameDir")
            }
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

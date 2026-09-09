package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.run.extension.RunSpec
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Unifies Fabric Loom run configs and ModDevGradle runs behind one
 * `modkit { run { } }` block.
 */
public class ModkitRunPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val model = project.applyModkitCore()
        val run = model.registerBlock(RUN_BLOCK, RunSpec::class.java)

        val context = RunContext(project, model, run)
        FEATURES.forEach { feature -> feature.install(context) }
    }

    private companion object {

        const val RUN_BLOCK: String = "run"

        val FEATURES: List<PluginFeature<RunContext>> = listOf(
            RunDefaults,
            RunInfo,
            RunDiagnostics,
            LoaderRuns,
        )
    }
}

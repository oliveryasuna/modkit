package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.mapping.RunConfigValues
import com.oliveryasuna.modkit.run.mapping.mapRunConfigToLoom
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.configuration.ide.RunConfigSettings
import org.gradle.api.Project

/**
 * Fabric Loom backend.
 *
 * Loom is applied by the `loaders` plugin at `plugins { }` time, before the
 * `run` DSL runs, so reads are deferred to `afterEvaluate`. Loom reads each
 * run's arguments lazily when it builds it run tasks, so mutating them there is
 * honored. Only `client`/`server` exists on Loom; `data`/`gametest` are warned
 * and skipped.
 */
internal object FabricRunBackend : RunBackend {

    override fun configure(ctx: RunContext) {
        TODO("Not yet implemented")
    }

    /**
     * Realizes one Loom run.
     *
     * `data`/`gametest` have no Loom helper, so they are warned and skipped.
     */
    private fun createRun(
        project: Project,
        loom: LoomGradleExtensionAPI,
        name: String,
        kind: RunKind,
        values: RunConfigValues
    ) {
        if(!values.enabled) return

        val setSide: (RunConfigSettings) -> Unit = when(kind) {
            RunKind.CLIENT -> RunConfigSettings::client
            RunKind.SERVER -> RunConfigSettings::server
            RunKind.DATA, RunKind.GAMETEST -> {
                warnUnsupported(project, name)
                return
            }
        }

        val mapping = mapRunConfigToLoom(name, values)
        mapping.warnings.forEach { project.logger.warn(it) }

        val settings = loom.runs.maybeCreate(name)
        setSide(settings)
        settings.runDir(mapping.runDir)
        if(mapping.vmArgs.isNotEmpty()) settings.vmArgs(mapping.vmArgs)
        if(mapping.programArgs.isNotEmpty()) settings.programArgs(mapping.programArgs)
    }

    private fun warnUnsupported(
        project: Project,
        name: String
    ) {
        project.logger.warn(
            "modkit.run: '$name' runs are not supported on Fabric (Loom has no run helper for that kind); skipping.",
        )
    }

}

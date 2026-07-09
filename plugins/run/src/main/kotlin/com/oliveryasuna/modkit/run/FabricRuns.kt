package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project

/**
 * Configures Fabric Loom run configs from the unified model. Kept internal — no
 * Loom types leak into run's public API.
 *
 * Loom is applied by the loaders plugin at `plugins { }` time (eagerly), so the
 * `fabric-loom` hook fires before the `modkit.run { }` DSL runs. Reading the
 * run config values is therefore deferred to `afterEvaluate`, once the DSL has
 * populated the model. Loom reads each run's arguments lazily when it builds
 * its run tasks, so mutating them in `afterEvaluate` is honored.
 */
internal fun configureFabricRuns(project: Project, run: RunSpec) {
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class.java)

    project.afterEvaluate {
        configureLoomRun(project, loom, "client", run.client) { it.client() }
        configureLoomRun(project, loom, "server", run.server) { it.server() }

        // Loom has no data-generation or gametest run helper — warn and skip
        // when either is explicitly enabled.
        if(run.data.enabled.getOrElse(false)) {
            project.logger.warn(
                "modkit.run: 'data' runs are not supported on Fabric " +
                "(Loom has no data-generation run helper); skipping."
            )
        }
        if(run.gametest.enabled.getOrElse(false)) {
            project.logger.warn(
                "modkit.run: 'gametest' runs are not supported on Fabric " +
                "(Loom has no gametest run helper); skipping."
            )
        }
    }
}

private fun configureLoomRun(
    project: Project,
    loom: LoomGradleExtensionAPI,
    name: String,
    config: RunConfig,
    setSide: (net.fabricmc.loom.configuration.ide.RunConfigSettings) -> Unit
) {
    val values = config.snapshot()
    if(!values.enabled) return

    val mapping = mapRunConfigToLoom(name, values)
    mapping.warnings.forEach { project.logger.warn(it) }

    val settings = loom.runs.maybeCreate(name)
    setSide(settings)
    settings.runDir(mapping.runDir)
    if(mapping.vmArgs.isNotEmpty()) {
        settings.vmArgs(mapping.vmArgs)
    }
    if(mapping.programArgs.isNotEmpty()) {
        settings.programArgs(mapping.programArgs)
    }
}

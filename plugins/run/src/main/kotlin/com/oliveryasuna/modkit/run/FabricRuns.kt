package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunSpec
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.configuration.ide.RunConfigSettings
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
        // Fixed runs.
        createLoomRun(project, loom, "client", RunKind.CLIENT, run.client.snapshot())
        createLoomRun(project, loom, "server", RunKind.SERVER, run.server.snapshot())
        if(run.data.enabled.getOrElse(false)) warnUnsupported(project, "data")
        if(run.gametest.enabled.getOrElse(false)) warnUnsupported(project, "gametest")

        // Compat-test variants.
        configureFabricVariants(project, loom, run)
    }
}

private fun configureFabricVariants(project: Project, loom: LoomGradleExtensionAPI, run: RunSpec) {
    run.variants.forEach { variant ->
        if(!variant.enabled.getOrElse(true)) return@forEach

        val syncTask = stageVariantMods(project, variant)

        variant.appliesToRuns.getOrElse(emptySet()).forEach { kindName ->
            val kind = RunKind.fromName(kindName)
            if(kind == null) {
                project.logger.warn("modkit.run: variant '${variant.name}' applies to unknown run kind '$kindName'; skipping.")
                return@forEach
            }

            // Clone the base run's values into the variant's own game directory
            // (forced enabled), plus the variant's extra args merged in.
            val values = run.runByKind(kind).snapshot().mergeVariant(
                gameDir = variant.gameDir.get(),
                jvmArgs = variant.jvmArgs.getOrElse(emptyList()),
                programArgs = variant.programArgs.getOrElse(emptyList()),
                systemProperties = variant.systemProperties.getOrElse(emptyMap()),
                environment = variant.environment.getOrElse(emptyMap())
            )
            val runName = kind.runName(variant.name)

            createLoomRun(project, loom, runName, kind, values)
            wireSyncDependency(project, runName, syncTask)
            aggregateVariantRun(project, runName)
        }
    }
}

/**
 * Realizes one Loom run of [kind]. Only `client`/`server` exist on Loom;
 * `data`/ `gametest` have no Loom run helper and are warned + skipped (matching
 * the base runs).
 */
private fun createLoomRun(
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
    if(mapping.vmArgs.isNotEmpty()) {
        settings.vmArgs(mapping.vmArgs)
    }
    if(mapping.programArgs.isNotEmpty()) {
        settings.programArgs(mapping.programArgs)
    }
}

private fun warnUnsupported(project: Project, name: String) {
    project.logger.warn(
        "modkit.run: '$name' runs are not supported on Fabric (Loom has no run helper for that kind); skipping."
    )
}

package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.modkitDiagnostics
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Exposes a unified `modkit.run { }` DSL and maps it onto whichever loader base
 * the loaders plugin applied. This plugin never applies a base itself — it
 * reacts to Fabric Loom / ModDevGradle via `withPlugin`, configuring the run
 * container of the one that is present.
 */
public class ModkitRunPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `run` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `run` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val run = modkit.registerBlock("run", RunSpec::class.java)

        applyConventions(run)
        registerRunInfo(project, run)
        publishDiagnostics(project, run)

        // Scope by whichever base is applied — only one ever is. run configures
        // the base's run container but never applies the base.
        project.pluginManager.withPlugin("fabric-loom") {
            configureFabricRuns(project, run)
        }
        project.pluginManager.withPlugin("net.neoforged.moddev") {
            configureNeoForgeRuns(project, run)
        }
    }

    /**
     * Publishes the run configuration + variants as a `modkitDoctor` section.
     */
    private fun publishDiagnostics(project: Project, run: RunSpec) {
        project.modkitDiagnostics().sections.put(
            "Runs",
            project.provider {
                buildList<String> {
                    listOf(
                        "client" to run.client,
                        "server" to run.server,
                        "data" to run.data,
                        "gametest" to run.gametest
                    ).forEach { (name, config) ->
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
        )
    }

    private fun applyConventions(run: RunSpec) {
        conventionRun(run.client, "run/client", enabled = true)
        conventionRun(run.server, "run/server", enabled = true)
        conventionRun(run.data, "run/data", enabled = false)
        conventionRun(run.gametest, "run/gametest", enabled = false)

        run.hotswap.preferJetBrainsRuntime.convention(true)

        // Variant conventions applied as elements are registered.
        run.variants.all { variant ->
            variant.gameDir.convention("run/${variant.name}")
            variant.enabled.convention(true)
        }
    }

    private fun conventionRun(config: RunConfig, gameDir: String, enabled: Boolean) {
        config.gameDir.convention(gameDir)
        config.enabled.convention(enabled)
        config.auth.convention(false)
    }

    private fun registerRunInfo(project: Project, run: RunSpec) {
        project.tasks.register("modkitRunInfo", ModkitRunInfoTask::class.java) { task ->
            task.group = "modkit"
            task.description = "Prints the resolved run configurations and reports hot-swap (JBR/DCEVM) status."

            // Snapshot inputs at configuration time for configuration-cache
            // compatibility; the JVM probe stays in the task action.
            task.runSummaries.set(project.provider { summarize(run) })
            task.preferJetBrainsRuntime.set(run.hotswap.preferJetBrainsRuntime)
        }
    }

    private fun summarize(run: RunSpec): List<String> =
        listOf(
            "client" to run.client,
            "server" to run.server,
            "data" to run.data,
            "gametest" to run.gametest
        ).map { (name, config) ->
            val values = config.snapshot()
            "$name: enabled=${values.enabled} gameDir=${values.gameDir} " +
            "jvmArgs=${values.jvmArgs} programArgs=${values.programArgs} " +
            "systemProperties=${values.systemProperties} environment=${values.environment} auth=${values.auth}"
        }

}

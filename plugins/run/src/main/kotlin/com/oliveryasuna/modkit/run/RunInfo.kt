package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.MODKIT_TASK_GROUP
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_PREFIX
import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.run.extension.RunSpec
import com.oliveryasuna.modkit.run.mapping.snapshot
import com.oliveryasuna.modkit.run.task.ModkitRunInfoTask

/**
 * Registers `modkitRunInfo`, which prints the resolved runs and reports
 * hot-swap (JBR/DCEVM) status. The summaries are snapshotted at configuration
 * time; the JVM probe stays in the task action.
 */
internal object RunInfo : PluginFeature<RunContext> {

    private const val TASK_NAME: String = "${MODKIT_TASK_PREFIX}RunInfo"

    override fun install(context: RunContext) {
        context.project.tasks.register(TASK_NAME, ModkitRunInfoTask::class.java) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Prints the resolved run configurations and reports hot-swap (JBR/DCEVM) status."
            task.runSummaries.set(context.project.provider { summarize(context.run) })
            task.preferJetBrainsRuntime.set(context.run.hotswap.preferJetBrainsRuntime)
        }
    }

    private fun summarize(run: RunSpec): List<String> =
        fixedRuns(run).map { (name, config) ->
            val values = config.snapshot()
            "$name: enabled=${values.enabled} gameDir=${values.gameDir} " +
                    "jvmArgs=${values.jvmArgs} programArgs=${values.programArgs} " +
                    "systemProperties=${values.systemProperties} environment=${values.environment} auth=${values.auth}"
        }
}

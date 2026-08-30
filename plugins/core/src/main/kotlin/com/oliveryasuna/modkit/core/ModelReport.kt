package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.plugin.MODKIT_TASK_GROUP
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_PREFIX
import com.oliveryasuna.modkit.plugin.PluginContext
import com.oliveryasuna.modkit.plugin.PluginFeature

private const val MODEL_TASK_NAME: String = "${MODKIT_TASK_PREFIX}Model"

/**
 * Registers `modkitModel`, a quick read-out of what the model resolved to.
 *
 * Useful for eyeballing ids, versions, and targets without kicking off a build.
 */
internal object ModelReport : PluginFeature<PluginContext> {

    override fun install(ctx: PluginContext) {
        val model = ctx.model

        ctx.project.tasks.register(MODEL_TASK_NAME) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Prints the resolved Modkit model (id, version, targets, toolchain)."

            // Snapshot everything the action needs, so at execution time it
            // touches neither the model nor the project.
            val modId = model.modId
            val version = model.version
            val toolchain = model.jvm.toolchain
            val targets = model.targets.map { "${it.minecraftVersion} -> ${it.loaders.get()}" }

            task.doLast {
                println("modId:     ${modId.orNull}")
                println("version:   ${version.orNull}")
                println("toolchain: ${toolchain.orNull}")
                println("targets:")
                targets.forEach { println("  $it") }
            }
        }
    }

}

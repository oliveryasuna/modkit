package com.oliveryasuna.modkit.scaffold

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the `modkitInit` task, which generates a working Modkit consumer
 * project. Applies no other plugins and does not create the Modkit model — it
 * only writes text. Task inputs are wired from `-P` flags, with sensible
 * defaults; `-PmodId` is the only required flag.
 *
 * Recognized flags:
 * `-PmodId -Pgroup -Pversions=1.21.1,1.20.1 -Ploaders=fabric,neoforge`
 * `-Pmodules=metadata,mixins,run -PtargetDir=<dir> -Pforce`.
 */
public class ModkitScaffoldPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.register("modkitInit", ModkitInitTask::class.java) { task ->
            task.group = "modkit"
            task.description = "Generates a working Modkit consumer project from the given flags."

            // It writes into a user tree and enforces its own overwrite guard,
            // so it must always run — never be skipped as UP-TO-DATE.
            task.outputs.upToDateWhen { false }

            // modId — required; validation happens in the task action so a bare
            // `modkitInit` still configures cleanly and fails with guidance.
            csv(target, "modId").singleOrNull()?.let { task.modId.set(it) }

            csv(target, "group").singleOrNull()?.let { task.modGroup.set(it) }

            csv(target, "versions").takeIf { it.isNotEmpty() }?.let { task.versions.set(it) }
            csv(target, "loaders").takeIf { it.isNotEmpty() }?.let { task.loaders.set(it) }
            csv(target, "modules").takeIf { it.isNotEmpty() }?.let { task.modules.set(it) }

            task.force.set(target.hasProperty("force"))

            val targetDirProp = prop(target, "targetDir")
            if(targetDirProp != null) {
                task.targetDir.set(target.layout.projectDirectory.dir(targetDirProp))
            } else {
                task.targetDir.set(target.layout.projectDirectory)
            }
        }
    }

    private fun prop(project: Project, name: String): String? =
        (project.findProperty(name) as String?)?.trim()?.takeIf { it.isNotEmpty() }

    /** Splits a comma-separated `-P` value into trimmed, non-blank tokens. */
    private fun csv(project: Project, name: String): List<String> =
        prop(project, name)
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
        ?: emptyList()
}

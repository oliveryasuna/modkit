package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.diagnostics.ModkitDiagnostics
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.Project

private const val DOCTOR_TASK_NAME: String = "${MODKIT_TASK_PREFIX}Doctor"

/**
 * Registers `modkitDoctor` and files `core`'s own entry in the shared
 * [ModkitDiagnostics] registry.
 *
 * The registry is the point: every applied plugin drops in a section (and any
 * problems), and the doctor prints all of them together. `core` owns the
 * "Model" section and the model-level problems. It only ever reports; a problem
 * is a warning, never a build failure.
 */
internal object ModkitDoctor : PluginFeature<PluginContext> {

    private const val MODEL_SECTION_TITLE: String = "Model"

    override fun install(ctx: PluginContext) {
        val project = ctx.project
        val diagnostics = project.modkitDiagnostics()

        contributeModelSection(project, ctx.model, diagnostics)
        registerDoctorTask(project, diagnostics)
    }

    private fun contributeModelSection(
        project: Project,
        model: ModkitExtension,
        diagnostics: ModkitDiagnostics
    ) {
        val modId = model.modId
        val group = model.group
        val version = model.version
        val toolchain = model.jvm.toolchain

        // A live provider, not an eager map: the container is still empty at
        // apply time, so read it when the report actually runs.
        val targets = project.provider {
            model.targets.map { "${it.minecraftVersion} -> ${it.loaders.get()}" }
        }

        diagnostics.sections.put(
            MODEL_SECTION_TITLE,
            project.provider {
                buildList {
                    add("modId:     ${modId.orNull ?: "(unset)"}")
                    add("group:     ${group.orNull ?: "(unset)"}")
                    add("version:   ${version.orNull ?: "(unset)"}")
                    add("toolchain: Java ${toolchain.orNull ?: "(unresolved)"}")
                    val declared = targets.get()
                    add("targets:${if(declared.isEmpty()) "   (none)" else ""}")
                    declared.forEach { add("  - $it") }
                }
            },
        )

        diagnostics.problems.addAll(
            project.provider {
                buildList {
                    if(targets.get().isEmpty()) add("No Minecraft targets declared; nothing will build.")
                }
            },
        )
    }

    private fun registerDoctorTask(
        project: Project,
        diagnostics: ModkitDiagnostics
    ) {
        val sections = diagnostics.sections
        val problems = diagnostics.problems

        project.tasks.register(DOCTOR_TASK_NAME) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Reports a health summary of the Modkit configuration across all applied plugins."

            task.doLast {
                val resolvedSections = sections.get()
                val resolvedProblems = problems.get()

                println("Modkit doctor")
                resolvedSections.forEach { (title, lines) ->
                    println()
                    println("[$title]")
                    lines.forEach { line -> println("  $line") }
                }
                println()
                println("[Problems]")
                if(resolvedProblems.isEmpty()) {
                    println("  none")
                } else {
                    resolvedProblems.forEach { problem -> println("  ! $problem") }
                }
            }
        }
    }

}

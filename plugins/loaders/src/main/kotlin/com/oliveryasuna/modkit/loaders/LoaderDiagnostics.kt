package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_GROUP
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_PREFIX
import com.oliveryasuna.modkit.plugin.modkitDiagnostics
import org.gradle.api.Project


private const val LOADER_INFO_TASK_NAME: String = "${MODKIT_TASK_PREFIX}LoaderInfo"

private const val DOCTOR_SECTION_TITLE: String = "Loader"

/**
 * The loader plugin's reporting: a `modkitLoaderInfo` task for a quick
 */
internal object LoaderDiagnostics {

    fun install(
        project: Project,
        model: ModkitExtension,
        loaders: LoadersSpec,
        activeLoader: ModLoader?
    ) {
        registerInfoTask(project, model, loaders, activeLoader)
        publishDoctorSection(project, loaders, activeLoader)
    }

    private fun registerInfoTask(
        project: Project,
        model: ModkitExtension,
        loaders: LoadersSpec,
        activeLoader: ModLoader?
    ) {
        project.tasks.register(LOADER_INFO_TASK_NAME) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Prints the resolved loader configuration (mappings, versions, targets)."

            // Snapshot at configuration time so the action stays cache safe.
            val scheme = loaders.mappings.scheme
            val parchment = loaders.mappings.parchment
            val fabricLoader = loaders.fabric.loaderVersion
            val fabricApi = loaders.fabric.apiVersion
            val neoforge = loaders.neoforge.version
            val targets = model.targets.map { "${it.minecraftVersion} -> ${it.loaders.get()}" }

            task.doLast {
                println("loader:    ${activeLoader?.name ?: "<not set> (set -P${ModLoader.PROPERTY}=fabric|neoforge)"}")
                println("mappings:  ${scheme.orNull}")
                println("parchment: ${parchment.orNull}")
                println("fabric:    loader=${fabricLoader.orNull} api=${fabricApi.orNull}")
                println("neoforge:  ${neoforge.orNull}")
                println("targets:")
                targets.forEach { println("  $it") }
            }
        }
    }

    private fun publishDoctorSection(
        project: Project,
        loaders: LoadersSpec,
        activeLoader: ModLoader?
    ) {
        val diagnostics = project.modkitDiagnostics()

        val scheme = loaders.mappings.scheme
        val parchment = loaders.mappings.parchment
        val fabricLoader = loaders.fabric.loaderVersion
        val fabricApi = loaders.fabric.apiVersion
        val neoforge = loaders.neoforge.version

        diagnostics.sections.put(
            DOCTOR_SECTION_TITLE,
            project.provider {
                listOf(
                    "active:    ${activeLoader?.name ?: "<not set>"}",
                    "mappings:  ${scheme.orNull}${parchment.orNull?.let { " + parchment $it" } ?: ""}",
                    "fabric:    loader=${fabricLoader.orNull ?: "-"} api=${fabricApi.orNull ?: "-"}",
                    "neoforge:  ${neoforge.orNull ?: "-"}",
                )
            },
        )

        if(activeLoader == null) {
            diagnostics.problems.add(
                "No active loader. Set -P${ModLoader.PROPERTY}=fabric|neoforge, or nothing will build.",
            )
        }
    }

}

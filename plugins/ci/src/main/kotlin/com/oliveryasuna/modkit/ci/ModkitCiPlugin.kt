package com.oliveryasuna.modkit.ci

import com.oliveryasuna.modkit.ci.extension.CiSpec
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.plugin.wireIntoCheck
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

public class ModkitCiPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `ci` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `ci` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val ci = modkit.registerBlock("ci", CiSpec::class.java)

        ci.provider.convention("github")
        ci.matrixFromTargets.convention(true)
        ci.java.convention(modkit.jvm.toolchain)
        ci.cache.convention(true)
        ci.publishOnTag.convention(true)
        ci.secrets.modrinth.convention("MODRINTH_TOKEN")
        ci.secrets.curseforge.convention("CURSEFORGE_TOKEN")
        ci.secrets.github.convention("GITHUB_TOKEN")

        // Capture a plain boolean snapshot of whether the publish plugin is
        // present — the generator gates the publish job on it, without holding
        // a reference to the plugin itself.
        val publishApplied = project.objects.property(Boolean::class.java).convention(false)
        project.pluginManager.withPlugin("com.oliveryasuna.modkit.publish") {
            publishApplied.set(true)
        }

        val matrix: Provider<List<CiMatrixEntry>> = project.provider {
            if(!ci.matrixFromTargets.get()) return@provider emptyList<CiMatrixEntry>()
            modkit.targets
                .filter { it.enabled.get() }
                .flatMap { target ->
                    target.loaders.get().map { loader ->
                        CiMatrixEntry(target.minecraftVersion, loader.name.lowercase())
                    }
                }
        }

        val workflowFile = project.layout.projectDirectory.file(".github/workflows/ci.yml")

        project.tasks.register("generateCiWorkflows", GenerateCiWorkflowsTask::class.java) { task ->
            task.group = "modkit"
            task.description = "Generates the GitHub Actions CI workflow from the Modkit model."
            configureInputs(task, ci, matrix, publishApplied)
            task.outputFile.set(workflowFile)
        }

        val verify = project.tasks.register("verifyCiWorkflows", VerifyCiWorkflowsTask::class.java) { task ->
            task.group = "verification"
            task.description = "Verifies the committed CI workflow matches the Modkit model."
            configureInputs(task, ci, matrix, publishApplied)
            task.committedFile.set(workflowFile)
        }

        project.wireIntoCheck(verify)
    }

    private fun configureInputs(
        task: CiWorkflowTask,
        ci: CiSpec,
        matrix: Provider<List<CiMatrixEntry>>,
        publishApplied: Provider<Boolean>
    ) {
        task.provider.set(ci.provider)
        task.matrix.set(matrix)
        task.java.set(ci.java)
        task.cache.set(ci.cache)
        task.publishOnTag.set(ci.publishOnTag)
        task.publishApplied.set(publishApplied)
        task.modrinthSecret.set(ci.secrets.modrinth)
        task.curseforgeSecret.set(ci.secrets.curseforge)
        task.githubSecret.set(ci.secrets.github)
    }

}

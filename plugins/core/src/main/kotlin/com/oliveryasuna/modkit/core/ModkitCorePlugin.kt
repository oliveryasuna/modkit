package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.common.toolchain.JavaToolchainResolver
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

public class ModkitCorePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("modkit", ModkitExtension::class.java)

        with(extension) {
            group.convention(project.provider { project.group.toString() })
            version.convention(project.provider { project.version.toString() })
            displayName.convention(modId)
            authors.convention(emptyList())

            layout.commonSourceSet.convention("main")
            layout.splitClient.convention(false)
        }

        // Per-target defaults applied as elements are created.
        extension.targets.all { target ->
            target.enabled.convention(true)
        }

        extension.jvm.toolchain.convention(
            project.provider {
                JavaToolchainResolver.resolveForTargets(
                    extension.targets.map { it.minecraftVersion }
                )
            }
        )

        project.pluginManager.withPlugin("java-base") {
            val java = project.extensions.getByType(JavaPluginExtension::class.java)
            java.toolchain.languageVersion.set(
                extension.jvm.toolchain.map { JavaLanguageVersion.of(it) }
            )
        }

        registerDiagnostics(project, extension)
        registerValidation(project, extension)
    }

    private fun registerDiagnostics(
        project: Project,
        extension: ModkitExtension
    ) {
        project.tasks.register("modkitModel") { task ->
            task.group = "modkit"
            task.description = "Prints the resolved Modkit model (id, version, targets, toolchain)."

            // Capture values at configuration time for configuration-cache
            // compatibility.
            val modId = extension.modId
            val version = extension.version
            val toolchain = extension.jvm.toolchain
            val targetsList = extension.targets.map { target ->
                "${target.minecraftVersion} -> ${target.loaders.get()}"
            }

            task.doLast {
                println("modId:     ${modId.orNull}")
                println("version:   ${version.orNull}")
                println("toolchain: ${toolchain.orNull}")
                println("targets:")
                targetsList.forEach { println("  $it") }
            }
        }
    }

    private fun registerValidation(
        project: Project,
        extension: ModkitExtension
    ) {
        val validate = project.tasks.register("validateModkitModel") { task ->
            task.group = "verification"
            task.description = "Validates the resolved Modkit model (modId, targets, loaders)."

            // Snapshot the model into plain, serializable values at
            // configuration time — no Project/extension capture in the action.
            val strict = STRICT
            val modId = extension.modId
            val targets = extension.targets.map { target -> ModkitModelValidator.TargetView(target.name, target.loaders.get().isNotEmpty()) }

            task.doLast {
                val errors = ModkitModelValidator.validate(modId.orNull, targets)
                if(errors.isNotEmpty()) {
                    val report = buildString {
                        append("Invalid Modkit model:")
                        errors.forEach { append("\n  - ").append(it) }
                    }
                    if(strict) throw GradleException(report) else task.logger.warn(report)
                }
            }
        }

        // Attach to `check` only where a lifecycle exists; `core` never applies
        // one.
        project.pluginManager.withPlugin("lifecycle-base") {
            project.tasks.named("check") { it.dependsOn(validate) }
        }
    }

    private companion object {

        // Strict validation fails the build; non-strict warns. Internal for
        // now — no public toggle in `core`.
        private const val STRICT: Boolean = true

    }

}

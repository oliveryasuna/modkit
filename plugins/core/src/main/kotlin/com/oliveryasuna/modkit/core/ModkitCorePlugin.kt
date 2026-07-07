package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class ModkitCorePlugin : Plugin<Project> {

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

        registerDiagnostics(project, extension)
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
            val targets = project.provider {
                extension.targets.map { target -> "${target.minecraftVersion} -> ${target.loaders.get()}" }
            }

            task.doLast {
                println("modId:     ${modId.orNull}")
                println("version:   ${version.orNull}")
                println("toolchain: ${toolchain.orNull}")
                println("targets:")
                targets.get().forEach { println("  $it") }
            }
        }
    }

}

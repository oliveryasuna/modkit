package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

/**
 * Loader-agnostic mod staging for a run variant: a resolvable, non-transitive
 * configuration holding the variant's extra mod coordinates, and a [Sync] task
 * that copies them into `<gameDir>/mods/` before the variant's runs launch.
 *
 * Non-transitive so exactly the declared mods are staged — no transitive
 * dependencies of a test mod leak into the run. Third-party mod repositories
 * (Modrinth/CurseMaven) are the `dependencies` plugin's job; if it is not
 * applied, the consumer adds the repository that hosts the mods.
 */
internal fun stageVariantMods(project: Project, variant: RunVariant): TaskProvider<Sync> {
    val capitalized = variant.name.replaceFirstChar { it.uppercaseChar() }

    val config = createStagingConfiguration(project, variant)

    // Capture the ProjectLayout (a config-cache-serializable service), never
    // `project`, into the `into` provider — otherwise the Sync task holds a
    // Project reference and fails configuration-cache serialization.
    val projectDir = project.layout.projectDirectory

    // Feed the Sync a lazy Provider of the *resolved artifact files*, not the
    // resolvable Configuration itself. Embedding the Configuration (or an
    // ArtifactView backed by it) in the copy spec drags the project's
    // repository handler — populated by Loom/loaders — into configuration-cache
    // serialization, which fails. `resolvedArtifacts` is the config-cache-safe
    // API: it carries only the resolved file references.
    val modJars = config.incoming.artifacts.resolvedArtifacts.map { artifacts ->
        artifacts.map { it.file }
    }

    return project.tasks.register("sync${capitalized}ToRun", Sync::class.java) { task ->
        task.group = "modkit"
        task.description = "Stages the '${variant.name}' variant's mods into its run mods/ directory."
        task.from(modJars)
        task.into(variant.gameDir.map { projectDir.dir(it).dir("mods") })
    }
}

private fun createStagingConfiguration(project: Project, variant: RunVariant): Configuration =
    project.configurations.maybeCreate("${variant.name}DevRuntime").apply {
        isCanBeResolved = true
        isCanBeConsumed = false
        isTransitive = false
        description = "Extra mod jars staged into the '${variant.name}' run variant's mods/ directory."
        // Resolves the variant's coordinates (including any `extends(...)`
        // chain) now that the DSL has run. Reading here is post-configuration,
        // not an apply-time eager read.
        variant.modCoordinates.get().forEach { coordinate ->
            dependencies.add(project.dependencies.create(coordinate))
        }
    }

/**
 * Makes the run task for [runName] depend on the variant's staging task, so the
 * extra mods are copied in before launch. Uses `matching` so it is robust to
 * whether the loader has registered the run task yet.
 */
internal fun wireSyncDependency(project: Project, runName: String, syncTask: TaskProvider<Sync>) {
    val taskName = "run" + runName.replaceFirstChar { it.uppercaseChar() }
    project.tasks.matching { it.name == taskName }.configureEach { it.dependsOn(syncTask) }
}

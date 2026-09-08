package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.MODKIT_TASK_GROUP
import com.oliveryasuna.modkit.run.extension.RunVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

private const val RUN_ALL_VARIANTS: String = "runAllVariants"

/**
 * Loader-agnostic mod staging for a variant: a resolvable, non-transitive
 * configuration holding the variant's extra mods, and a [Sync] that copies them
 * into `<gameDir>/mods/` before the variant's runs launch.
 *
 * Non-transitive so exactly the declared mods are staged, with none of their
 * transitive dependencies leaking into the run. The repositories that host those
 * mods (Modrinth/CurseMaven) are the `dependencies` plugin's job; without it,
 * the consumer adds them.
 */
internal fun stageVariantMods(
    project: Project,
    variant: RunVariant
): TaskProvider<Sync> {
    val capitalized = variant.name.replaceFirstChar { it.uppercaseChar() }
    val staging = stagingConfiguration(project, variant)

    // Capture the ProjectLayout (a cache-serializable service), never
    // `project`, for the `into` provider, or the Sync task would hold a Project
    // reference and fail configuration-cache serialization.
    val projectDir = project.layout.projectDirectory

    // Feed the Sync a lazy provider of the *resolved artifact files*, not the
    // configuration itself. Putting the Configuration (or an ArtifactView over
    // it) in the copy spec drags the Loom/loaders-populated repository handler
    // into cache serialization, which fails. resolvedArtifacts carries only
    // file references, and is the cache-safe way in.
    val modJars = staging.incoming.artifacts.resolvedArtifacts.map { artifacts ->
        artifacts.map { it.file }
    }

    return project.tasks.register("sync${capitalized}ToRun", Sync::class.java) { task ->
        task.group = MODKIT_TASK_GROUP
        task.description = "Stages the '${variant.name}' variant's mods into its run mods/ directory."
        task.from(modJars)
        task.into(variant.gameDir.map { projectDir.dir(it).dir("mods") })
    }
}

private fun stagingConfiguration(
    project: Project,
    variant: RunVariant
): Configuration =
    project.configurations.maybeCreate("${variant.name}DevRuntime").apply {
        isCanBeResolved = true
        isCanBeConsumed = false
        isTransitive = false
        description = "Extra mod jars staged into the '${variant.name}' run variant's mods/ directory."
        // Resolves the variant's coordinates (including any `extends(...)`
        // chain) now that the DSL has run. This is post-configuration, not an
        // eager read.
        variant.modCoordinates.get().forEach { coordinate ->
            dependencies.add(project.dependencies.create(coordinate))
        }
    }

/**
 * Makes the run task for [runName] depend on [syncTask], so the extra mods land
 * before launch. `matching` keeps this robust to whether the loader has
 * registered the run task yet.
 */
internal fun wireSyncDependency(
    project: Project,
    runName: String,
    syncTask: TaskProvider<Sync>
) {
    val runTaskName = runTaskName(runName)
    project.tasks.matching { it.name == runTaskName }.configureEach { it.dependsOn(syncTask) }
}

/**
 * Adds the variant run [runName] to the aggregate `runAllVariants` task,
 * creating that task on first use. Running it launches every variant's run in
 * turn.
 */
internal fun aggregateVariantRun(
    project: Project,
    runName: String
) {
    if(project.tasks.findByName(RUN_ALL_VARIANTS) == null) {
        project.tasks.register(RUN_ALL_VARIANTS) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Runs every compatibility-test variant's run, in sequence."
        }
    }

    val runTaskName = runTaskName(runName)
    project.tasks.named(RUN_ALL_VARIANTS) { it.dependsOn(project.tasks.matching { task -> task.name == runTaskName }) }
}

/**
 * The Loom/MDG run task name for a run named [runName], e.g.,
 * `clientModMenu` -> `runClientModMenu`.
 */
private fun runTaskName(runName: String): String =
    "run" + runName.replaceFirstChar { it.uppercaseChar() }

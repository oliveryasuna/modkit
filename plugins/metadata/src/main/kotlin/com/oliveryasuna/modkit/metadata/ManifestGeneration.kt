package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.metadata.task.GenerateFabricModJsonTask
import com.oliveryasuna.modkit.metadata.task.GenerateManifestTask
import com.oliveryasuna.modkit.metadata.task.GenerateNeoForgeTomlTask
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

/**
 * Registers the generate task for the active loader and gets its output
 * packaged.
 *
 * Only the active loader's manifest is built, since a build targets one loader
 * at a time. Both loaders share the same task wiring, so a single reified
 * helper registers whichever concrete task the loader calls for.
 */
internal object ManifestGeneration : PluginFeature<MetadataContext> {

    private const val FABRIC_TASK_NAME: String = "generateFabricModJson"
    private const val NEOFORGE_TASK_NAME: String = "generateNeoForgeToml"

    private const val OUTPUT_BASE: String = "${MODKIT_OUTPUT_DIR}/metadata"

    override fun install(ctx: MetadataContext) {
        val loader = ctx.activeLoader ?: return

        val generate = registerGenerateTask(ctx, loader)
        packageIntoResources(ctx, generate)
    }

    private fun registerGenerateTask(
        context: MetadataContext,
        loader: ModLoader
    ): TaskProvider<out GenerateManifestTask> =
        when(loader) {
            ModLoader.FABRIC -> register<GenerateFabricModJsonTask>(
                context = context,
                loader = loader,
                taskName = FABRIC_TASK_NAME,
                description = "Generates fabric.mod.json from the Modkit model.",
                rawOverrides = context.metadata.fabric.raw,
            )

            ModLoader.NEOFORGE -> register<GenerateNeoForgeTomlTask>(
                context = context,
                loader = loader,
                taskName = NEOFORGE_TASK_NAME,
                description = "Generates neoforge.mods.toml from the Modkit model.",
                rawOverrides = context.metadata.neoforge.raw,
            )
        }

    private inline fun <reified T : GenerateManifestTask> register(
        context: MetadataContext,
        loader: ModLoader,
        taskName: String,
        description: String,
        rawOverrides: MapProperty<String, Any>,
    ): TaskProvider<T> {
        val project = context.project
        val contributions = project.modkitManifestContributions()
        val outputDir = project.layout.buildDirectory.dir("${OUTPUT_BASE}/${loader.name.lowercase()}")

        return project.tasks.register(taskName, T::class.java) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = description
            task.bindModel(context.model, context.metadata, minecraftVersion(context, loader))
            task.rawOverrides.set(rawOverrides)
            task.mixinConfigs.set(contributions.mixinConfigs)
            task.fabricDatagenEntrypoints.set(contributions.fabricDatagenEntrypoints)
            task.outputDir.set(outputDir)
        }
    }

    /**
     * Minecraft version of the first enabled target that includes [loader], or
     * absent.
     */
    private fun minecraftVersion(
        context: MetadataContext,
        loader: ModLoader
    ): Provider<String> =
        context.project.provider {
            context.model.targets
                .firstOrNull { it.enabled.get() && loader in it.loaders.get() }
                ?.minecraftVersion
        }

    private fun packageIntoResources(
        context: MetadataContext,
        generate: TaskProvider<out GenerateManifestTask>
    ) {
        // The manifest belongs to the common source set. Add the generated dir
        // as a resource source, and wire processResources to the generate task
        // EXPLICITLY: once a loader base (Loom/MDG) rewires the resource
        // pipeline, the provider-based srcDir dependency is not reliably
        // inferred, and the jar would otherwise ship with no manifest.
        context.project.withCommonSourceSet(context.commonSourceSet) { common ->
            common.resources.srcDir(generate.flatMap { it.outputDir })
            context.project.tasks.named(common.processResourcesTaskName) { it.dependsOn(generate) }
        }
    }

}

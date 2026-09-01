package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.GameTarget
import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.loaders.accesstransformer.GenerateAccessTransformerTask
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_GROUP
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

/**
 * The NeoForge side, over ModDevGradle.
 *
 * MDG's `setVersion` is eager and wants the NeoForge version straight away, but
 * that only rrives once the model DSL has run. So, unlike Loom, MDG is applied
 * in `afterEvaluate`. The access-transformer task is registered eagerly
 * regardless, so its output is available whichever way that goes.
 */
internal object NeoForgeBase : LoaderBase {

    private const val MODDEV_PLUGIN_ID: String = "net.neoforged.moddev"

    private const val GENERATE_AT_TASK_NAME: String = "generateAccessTransformer"
    private const val AT_OUTPUT_PATH: String = "modkit/accesstransformer.cfg"

    private const val CLIENT_SOURCE_SET: String = "client"

    override fun install(context: LoaderContext) {
        val accessTransformer = registerAccessTransformerTask(context)

        context.project.afterEvaluate {
            applyModDevGradle(context, accessTransformer)
        }
    }

    private fun registerAccessTransformerTask(context: LoaderContext): TaskProvider<GenerateAccessTransformerTask> =
        context.project.tasks.register(GENERATE_AT_TASK_NAME, GenerateAccessTransformerTask::class.java) { task ->
            task.group = MODKIT_TASK_GROUP
            task.description = "Generates a NeoForge access transformer from the project's access wideners."
            task.accessWideners.from(context.loaders.accessWideners)
            task.accessTransformer.set(context.project.layout.buildDirectory.file(AT_OUTPUT_PATH))
        }

    private fun applyModDevGradle(
        context: LoaderContext,
        accessTransformer: TaskProvider<GenerateAccessTransformerTask>
    ) {
        val project = context.project
        val target = ActiveTarget.resolve(context.model, ModLoader.NEOFORGE)

        rejectYarn(context)
        val version = context.loaders.neoforge.version.orNull
            ?: throw GradleException("modkit.loader=neoforge requires modkit.loaders.neoforge.version to be set.")

        project.pluginManager.apply(MODDEV_PLUGIN_ID)
        project.addParchmentRepository()

        val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)
        neoForge.setVersion(version)

        // Register the mod against its common source set. MDG uses this to put
        // the mod's classes on the dev-run classpath; without it the mod is
        // scanned from the manifest but its mixin classes fail to resolve at
        // runtime  (NeoForge's mixin service dies in PREPARE with
        // ClassNotFoundException).
        val common = context.layout.requireCommonSourceSet(project)
        val mod = neoForge.mods.register(context.model.modId.get()) { it.sourceSet(common) }

        // task output -> the dependency carries the task ordering.
        neoForge.accessTransformers.from(accessTransformer.flatMap { it.accessTransformer })

        layerParchment(context, neoForge, target)

        if(context.layout.isSplitClient) {
            // Split-client requires the common set to be `main` (enforced in
            // SourceLayout), so `common` here is main. MDG has no native split,
            // so synthesize a `client` set that mirrors Fabric's: same modding
            // classpath, sees main's output, packaged into the mod jar.
            val client = project.sourceSets.create(CLIENT_SOURCE_SET)
            neoForge.addModdingDependenciesTo(client)
            client.compileClasspath += common.output
            client.runtimeClasspath += common.output
            mod.configure { it.sourceSet(client) }
            project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) { it.from(client.output) }
        }
    }

    private fun rejectYarn(context: LoaderContext) {
        if(context.loaders.mappings.scheme.get() == MappingsScheme.YARN) {
            throw GradleException("Yarn mappings are not supported on NeoForge (mojmap-native). Use scheme = MOJMAP.")
        }
    }

    /**
     * NeoForge is mojmap-native, so Parchment applies directly once a version
     * is present.
     */
    private fun layerParchment(
        context: LoaderContext,
        neoForge: NeoForgeExtension,
        target: GameTarget
    ) {
        val parchment = context.loaders.mappings.parchment.orNull ?: return

        neoForge.parchment.minecraftVersion.set(target.minecraftVersion)
        neoForge.parchment.mappingsVersion.set(parchment)
    }

}

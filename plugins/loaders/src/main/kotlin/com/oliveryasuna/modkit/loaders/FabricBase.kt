package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import java.io.File

/**
 * The Fabric side, over Fabric Loom.
 *
 * Loom is applied eagerly and configured through lazy providers, since the
 * model DSL only runs after this. The one thing that cannot wait is the
 * source-set shape: Loom finalizes its Minecraft jar configuration early, so
 * split-client and a non-`main` common set are wired up front, before
 * anything else.
 */
internal object FabricBase : LoaderBase {

    private const val LOOM_PLUGIN_ID: String = "fabric-loom"

    private const val MINECRAFT_CONFIGURATION: String = "minecraft"
    private const val MAPPINGS_CONFIGURATION: String = "mappings"
    private const val MOD_IMPLEMENTATION_CONFIGURATION: String = "modImplementation"

    private const val MINECRAFT_MODULE: String = "com.mojang:minecraft"
    private const val FABRIC_LOADER_MODULE: String = "net.fabricmc:fabric-loader"
    private const val FABRIC_API_MODULE: String = "net.fabricmc.fabric-api:fabric-api"
    private const val PARCHMENT_DATA_MODULE: String = "org.parchmentmc.data:parchment"

    override fun install(ctx: LoaderContext) {
        val project = ctx.project

        project.pluginManager.apply(LOOM_PLUGIN_ID)
        project.addParchmentRepository()

        val loom = project.extensions.getByType(LoomGradleExtensionAPI::class.java)

        configureSourceSets(ctx, loom)

        // The single Fabric target's Minecraft version, needed both as a
        // dependency and inside the Parchment coordinate below.
        val minecraftVersion = project.provider {
            ActiveTarget.resolve(ctx.model, ModLoader.FABRIC).minecraftVersion
        }

        loom.accessWidenerPath.fileProvider(singleAccessWidener(ctx))
        registerDependencies(ctx, minecraftVersion, mappings(ctx, loom, minecraftVersion))
    }

    private fun configureSourceSets(
        context: LoaderContext,
        loom: LoomGradleExtensionAPI
    ) {
        val layout = context.layout

        // Native client split. Must happen before Loom finalizes its config.
        if(layout.isSplitClient) {
            loom.splitEnvironmentSourceSets()
        }

        // `main` is Loom's default mod source set, so only a non-default one
        // needs wiring: give it the remap configurations and register it as the
        // mod's set.
        if(!layout.isCommonMain) {
            val common = layout.requireCommonSourceSet(context.project)
            loom.createRemapConfigurations(common)
            loom.mods.register(context.model.modId.get()) { it.sourceSet(common) }
        }
    }

    /**
     * Loom takes a single access widener, or none.
     *
     * More than one is a configuration error.
     */
    private fun singleAccessWidener(context: LoaderContext): Provider<File> =
        context.project.provider {
            val files = context.loaders.accessWideners.files

            require(files.size <= 1) {
                "Fabric Loom supports a single access widener, but ${files.size} were provided."
            }

            files.firstOrNull()
        }

    /**
     * The mappings dependency: mojmap, with Parchment layered on when a
     * version is set.
     */
    private fun mappings(
        context: LoaderContext,
        loom: LoomGradleExtensionAPI,
        minecraftVersion: Provider<String>
    ): Provider<Dependency> =
        context.project.provider {
            when(context.loaders.mappings.scheme.get()) {
                MappingsScheme.MOJMAP -> {
                    val parchment = context.loaders.mappings.parchment.orNull

                    if(parchment == null) {
                        loom.officialMojangMappings()
                    } else {
                        // TODO: Ensure this stays.
                        loom.layered {
                            it.officialMojangMappings()
                            // Parchment data ships as a zip with no POM, so the
                            // `@zip` classifier is required for Loom to resolve
                            // it
                            it.parchment("${PARCHMENT_DATA_MODULE}-${minecraftVersion.get()}:${parchment}@zip")
                        }
                    }
                }

                // TODO: Add support.
                MappingsScheme.YARN -> throw GradleException(
                    "Yarn mappings are not yet wired for the Fabric base (no curated yarn version source). " +
                            "Use scheme = MOJMAP."
                )
            }
        }

    private fun registerDependencies(
        context: LoaderContext,
        minecraftVersion: Provider<String>,
        mappings: Provider<Dependency>,
    ) {
        val fabric = context.loaders.fabric

        with(context.project.dependencies) {
            addProvider(MINECRAFT_CONFIGURATION, minecraftVersion.map { "${MINECRAFT_MODULE}:${it}" })
            addProvider(MAPPINGS_CONFIGURATION, mappings)
            addProvider(MOD_IMPLEMENTATION_CONFIGURATION, fabric.loaderVersion.map { "${FABRIC_LOADER_MODULE}:${it}" })
            // Fabric API is optional; an unset version maps to nothing and adds
            // no dependency.
            addProvider(MOD_IMPLEMENTATION_CONFIGURATION, fabric.apiVersion.map { "${FABRIC_API_MODULE}:${it}" })
        }
    }

}

package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Generate `fabric.mod.json` / `neoforge.mods.toml` from the shared model.
 *
 * `apply()` attaches the `metadata` block, gathers the eager bits into a
 * [MetadataContext], and installs its two features: generation and validation.
 * Defaults come first so the rest read a fully-conventioned block.
 */
public class ModkitMetadataPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val model = project.applyModkitCore()
        val metadata = model.registerBlock(METADATA_BLOCK, MetadataSpec::class.java)

        val context = MetadataContext(
            project = project,
            model = model,
            metadata = metadata,
            activeLoader = project.activeLoader(),
            commonSourceSet = project.commonSourceSet()
        )

        FEATURES.forEach { it.install(context) }
    }

    private companion object {

        const val METADATA_BLOCK: String = "metadata"

        val FEATURES: List<PluginFeature<MetadataContext>> = listOf(
            MetadataDefaults,
            ManifestGeneration,
            MetadataValidation,
        )
    }

}

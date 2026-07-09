package com.oliveryasuna.modkit.datagen

import com.oliveryasuna.modkit.datagen.extension.DatagenSpec
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Wires Minecraft data generation uniformly across loaders and routes generated
 * output into resources. A thin build over each loader's data run:
 *
 *  - **Fabric (Loom):** configures the `fabricApi` data generation (output dir,
 *    client toggle) — Loom registers the `runDatagen` task and auto-adds the
 *    output to resources. The Fabric `DataGeneratorEntrypoint` FQCN is
 *    published to the shared manifest registry so `metadata` lists it in
 *    fabric.mod.json.
 *  - **NeoForge (MDG):** configures the `data` run (output dir, mod id, run all
 *    providers) and registers the output as a main-resources source (minus
 *    `.cache`). Composes with the `run` plugin, which also targets the `data`
 *    run.
 *
 * Applying this plugin *is* the datagen enable switch — there is deliberately
 * no lazy `enabled` flag (see [DatagenSpec]). This plugin never applies a
 * loader base; it reacts to whichever base the loaders plugin applied via
 * `withPlugin`.
 */
public class ModkitDatagenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `datagen` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `datagen` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val datagen = modkit.registerBlock("datagen", DatagenSpec::class.java)

        // Conventions (set in apply(), never in the managed type).
        datagen.outputDir.convention(project.layout.projectDirectory.dir("src/main/generated"))
        datagen.includeClient.convention(true)

        // Scope by whichever base is applied — only one ever is. datagen
        // configures the base's data generation but never applies the base.
        project.pluginManager.withPlugin("fabric-loom") {
            configureFabricDatagen(project, modkit, datagen)
        }
        project.pluginManager.withPlugin("net.neoforged.moddev") {
            configureNeoForgeDatagen(project, modkit, datagen)
        }
    }

}

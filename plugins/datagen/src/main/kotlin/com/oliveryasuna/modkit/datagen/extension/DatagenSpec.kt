package com.oliveryasuna.modkit.datagen.extension

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * The `modkit.datagen { }` block. Declares a unified, cross-loader data
 * generation configuration that maps onto Fabric Loom's `fabricApi` data
 * generation and ModDevGradle's `data` run.
 *
 * There is intentionally **no** `enabled` flag: applying the datagen plugin
 * *is* the enable switch. A lazy `enabled` Property cannot gate the structural,
 * configuration-time calls this plugin makes into Loom/MDG (the same eager/lazy
 * trap that forced `modkit.loader`/`splitClient` and the dependency
 * repositories to be Gradle properties rather than DSL Properties).
 */
public abstract class DatagenSpec {

    /**
     * Directory the generators write into. Convention:
     * `src/main/generated`. On Fabric this becomes Loom's
     * `DataGenerationSettings.outputDirectory`; on NeoForge it is passed to the
     * `data` run via `--output` and registered as a generated-resources source.
     */
    public abstract val outputDir: DirectoryProperty

    /**
     * Fully-qualified name of the Fabric
     * `net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint`
     * implementation. Optional and **Fabric-only** — it is published to the
     * shared manifest registry so `metadata` folds it into fabric.mod.json
     * `entrypoints.fabric-datagen`. NeoForge discovers its data providers via
     * the mod event bus and needs no manifest entry.
     */
    public abstract val entrypoint: Property<String>

    /**
     * Whether to gather client-side data (models, blockstates, lang, …) in
     * addition to common/server data. Convention: `true`. On Fabric this sets
     * `DataGenerationSettings.client`; on NeoForge it selects the client data
     * run type where the target NeoForge version supports it.
     */
    public abstract val includeClient: Property<Boolean>

}

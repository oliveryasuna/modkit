package com.oliveryasuna.modkit.datagen

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.datagen.extension.DatagenSpec
import com.oliveryasuna.modkit.plugin.modkitManifestContributions
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import org.gradle.api.Project

/**
 * Wires Fabric data generation through Loom's `fabricApi` extension. Kept
 * internal — no Loom types leak into datagen's public API.
 *
 * Verified against fabric-loom 1.17.13 (bundled): the project extension
 * `fabricApi` is of type [FabricApiExtension];
 * `configureDataGeneration(Action)` takes a
 * `net.fabricmc.loom.api.fabricapi.DataGenerationSettings`, whose relevant
 * members are `outputDirectory: RegularFileProperty`,
 * `client: Property<Boolean>`, and `addToResources: Property<Boolean>`
 * (conventioned `true`). Loom registers the `runDatagen` task and — because
 * `addToResources` defaults `true` — adds the output dir to the main resources
 * itself, excluding the datagen `.cache` subtree. We therefore do NOT srcDir
 * the output on Fabric (that would double-register).
 *
 * All wiring is lazy: spec Properties are wired into Loom's Properties; nothing
 * is `.get()`-ed at configuration time.
 */
internal fun configureFabricDatagen(project: Project, modkit: ModkitExtension, datagen: DatagenSpec) {
    val fabricApi = project.extensions.getByType(FabricApiExtension::class.java)

    fabricApi.configureDataGeneration { settings ->
        // outputDirectory is a RegularFileProperty; wire the DirectoryProperty
        // through as a file provider (lazy).
        settings.outputDirectory.fileProvider(datagen.outputDir.map { it.asFile })
        settings.client.set(datagen.includeClient)
        // Leave addToResources at its `true` convention so Loom auto-adds the
        // generated dir (minus .cache/**) to main resources. Leave
        // createSourceSet at its `false` convention.
    }

    // Publish the Fabric datagen entrypoint FQCN to the shared manifest
    // registry. `metadata` folds it into fabric.mod.json
    // entrypoints.fabric-datagen. `entrypoint` is optional, so wire it lazily
    // via addAll(<provider of a possibly-empty set>) rather than
    // add(<provider>) — the latter errors when the source Provider is absent
    // (SetProperty.add requires the element to be present at realization).
    // orElse(emptySet()) makes an unset entrypoint contribute nothing.
    project.modkitManifestContributions().fabricDatagenEntrypoints
        .addAll(datagen.entrypoint.map { setOf(it) }.orElse(emptySet()))
}

package com.oliveryasuna.modkit.datagen

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.datagen.extension.DatagenSpec
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * Wires NeoForge data generation through ModDevGradle's `data` run. Kept
 * internal — no MDG types leak into datagen's public API.
 *
 * Verified against ModDevGradle 2.0.141 (bundled) + NeoForge 21.1.x:
 *
 *  - MDG's `RunModel` exposes `data()`, `clientData()` and `serverData()`, each
 *    of which only sets the run's *type* string. The type must name a run type
 *    declared in the NeoForge userdev `config.json`.
 *  - NeoForge **21.1.x** declares a single `data` run type (no `clientData` /
 *    `serverData`); those were introduced in NeoForge **21.4** where the plain
 *    `data` type was removed. For the pinned 1.21.1 / 21.1.x stack we therefore
 *    call `data()` (gathering client+common data). `includeClient` cannot flip
 *    to a separate client-data run type on 21.1.x, so it is a no-op on this
 *    NeoForge stack.
 *  - Neither MDG nor the NeoForge `data` run type auto-supplies `--output` /
 *    `--mod` / `--all`, and MDG does **not** auto-register the generated dir as
 *    a resource source. We therefore pass those program arguments ourselves and
 *    register the output as a main-resources srcDir, excluding the datagen
 *    `.cache` subdir so it does not churn up-to-date checks or get packaged.
 *
 * Composes with the `run` plugin: both target the run named `data` via
 * `runs.maybeCreate("data")`, so applying both plugins configures one run.
 *
 * All wiring is lazy — no `.get()` at configuration time.
 */
internal fun configureNeoForgeDatagen(project: Project, modkit: ModkitExtension, datagen: DatagenSpec) {
    val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)

    val run = neoForge.runs.maybeCreate("data")

    // Select the data run type. On NeoForge 21.1.x only `data` exists; it
    // gathers client + common data. (21.4+ split into clientData/serverData.)
    run.data()

    // Point the generator at the configured output directory and tell it which
    // mod to generate for and to run all providers. These args are added lazily
    // from the `datagen` block's properties.
    run.programArguments.add("--mod")
    run.programArguments.add(modkit.modId)
    run.programArguments.add("--all")
    run.programArguments.add("--output")
    run.programArguments.add(datagen.outputDir.map { it.asFile.absolutePath })

    // MDG does not auto-register the generated output as a resources source, so
    // pack it via processResources ourselves. Exclude the datagen `.cache`
    // subdir (written by the generator) so it is neither packaged nor treated
    // as an up-to-date input that churns. Lazy provider — no eager read.
    project.pluginManager.withPlugin("java-base") {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.getByName("main")
        main.resources.srcDir(datagen.outputDir)
        main.resources.exclude(".cache/**")
    }
}

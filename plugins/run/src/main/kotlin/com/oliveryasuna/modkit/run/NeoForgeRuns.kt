package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/** Applies the ModDevGradle run type for a [RunKind] to a run model. */
private fun RunKind.applyToNeoForge(model: RunModel) {
    when(this) {
        RunKind.CLIENT -> model.client()
        RunKind.SERVER -> model.server()
        RunKind.DATA -> model.data()
        RunKind.GAMETEST -> model.type.set("gameTestServer")
    }
}

/** The IntelliJ run-config name MDG surfaces for a [RunKind]. */
private val RunKind.ideName: String
    get() = when(this) {
        RunKind.CLIENT -> "Minecraft Client"
        RunKind.SERVER -> "Minecraft Server"
        RunKind.DATA -> "Minecraft Data"
        RunKind.GAMETEST -> "Minecraft Game Test"
    }

/**
 * Configures ModDevGradle runs from the unified model. Kept internal — no MDG
 * types leak into run's public API.
 *
 * MDG maps the unified config essentially one-to-one: it has native
 * system-property and environment-variable maps and a dev-login toggle. MDG is
 * applied by the loaders plugin inside its own `afterEvaluate`, so the
 * `net.neoforged.moddev` hook fires after the `modkit.run { }` DSL has run;
 * properties are wired lazily regardless.
 */
internal fun configureNeoForgeRuns(project: Project, run: RunSpec) {
    val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)

    // Fixed runs.
    configureNeoForgeRun(project, neoForge, "client", RunKind.CLIENT, run.client)
    configureNeoForgeRun(project, neoForge, "server", RunKind.SERVER, run.server)
    configureNeoForgeRun(project, neoForge, "data", RunKind.DATA, run.data)
    configureNeoForgeRun(project, neoForge, "gametest", RunKind.GAMETEST, run.gametest)

    // Compat-test variants.
    configureNeoForgeVariants(project, neoForge, run)
}

private fun configureNeoForgeRun(
    project: Project,
    neoForge: NeoForgeExtension,
    name: String,
    kind: RunKind,
    config: RunConfig
) {
    if(!config.enabled.getOrElse(false)) return
    writeNeoForgeRun(
        neoForge = neoForge,
        runName = name,
        kind = kind,
        base = config,
        gameDir = config.gameDir.map { project.layout.projectDirectory.dir(it) },
        ideName = kind.ideName
    )
}

private fun configureNeoForgeVariants(project: Project, neoForge: NeoForgeExtension, run: RunSpec) {
    run.variants.forEach { variant ->
        if(!variant.enabled.getOrElse(true)) return@forEach

        val syncTask = stageVariantMods(project, variant)

        variant.appliesToRuns.getOrElse(emptySet()).forEach { kindName ->
            val kind = RunKind.fromName(kindName)
            if(kind == null) {
                project.logger.warn("modkit.run: variant '${variant.name}' applies to unknown run kind '$kindName'; skipping.")
                return@forEach
            }

            val runName = kind.runName(variant.name)
            val model = writeNeoForgeRun(
                neoForge = neoForge,
                runName = runName,
                kind = kind,
                base = run.runByKind(kind),
                gameDir = variant.gameDir.map { project.layout.projectDirectory.dir(it) },
                ideName = "${kind.ideName} (${variant.name})"
            )
            // Layer the variant's extra args over the base's: lists append,
            // maps merge with the variant winning on a key collision (the later
            // putAll takes precedence).
            model.jvmArguments.addAll(variant.jvmArgs)
            model.programArguments.addAll(variant.programArgs)
            model.systemProperties.putAll(variant.systemProperties)
            model.environment.putAll(variant.environment)

            wireSyncDependency(project, runName, syncTask)
            aggregateVariantRun(project, runName)
        }
    }
}

/**
 * Writes a run of [kind] named [runName], taking its args from [base] but its
 * game dir from [gameDir]. Returns the model so callers (variants) can layer
 * extra args on top.
 */
private fun writeNeoForgeRun(
    neoForge: NeoForgeExtension,
    runName: String,
    kind: RunKind,
    base: RunConfig,
    gameDir: Provider<Directory>,
    ideName: String
): RunModel {
    val model = neoForge.runs.maybeCreate(runName)
    kind.applyToNeoForge(model)

    model.gameDirectory.set(gameDir)
    model.jvmArguments.addAll(base.jvmArgs)
    model.programArguments.addAll(base.programArgs)
    model.systemProperties.putAll(base.systemProperties)
    model.environment.putAll(base.environment)
    model.devLogin.set(base.auth)
    model.ideName.set(ideName)

    return model
}

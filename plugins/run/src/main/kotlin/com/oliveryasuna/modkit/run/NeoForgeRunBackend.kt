package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/**
 * The NeoForge side, over ModDevGradle.
 *
 * MDG maps the unified config almost one-to-one: it has native system-property and
 * environment maps and a dev-login toggle. MDG is applied inside the loaders
 * plugin's `afterEvaluate`, so this fires after the `run` DSL; args wire lazily
 * regardless.
 */
internal object NeoForgeRunBackend : RunBackend {

    override fun configure(ctx: RunContext) {
        val project = ctx.project
        val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)
        val run = ctx.run

        fixedRun(neoForge, "client", RunKind.CLIENT, run.client)
        fixedRun(neoForge, "server", RunKind.SERVER, run.server)
        fixedRun(neoForge, "data", RunKind.DATA, run.data)
        fixedRun(neoForge, "gametest", RunKind.GAMETEST, run.gametest)

        ctx.forEachVariantRun { variant, kind, runName ->
            val model = writeRun(
                neoForge = neoForge,
                runName = runName,
                kind = kind,
                base = run.runByKind(kind),
                gameDir = variant.gameDir,
                ideName = "${kind.ideName} (${variant.name})",
            )
            // Layer the variant's extras over the base: lists append, maps
            // merge with the variant winning (the later putAll takes
            // precedence).
            model.jvmArguments.addAll(variant.jvmArgs)
            model.programArguments.addAll(variant.programArgs)
            model.systemProperties.putAll(variant.systemProperties)
            model.environment.putAll(variant.environment)
        }
    }

    private fun fixedRun(
        neoForge: NeoForgeExtension,
        name: String,
        kind: RunKind,
        config: RunConfig
    ) {
        if(!config.enabled.getOrElse(false)) return
        writeRun(
            neoForge = neoForge,
            runName = name,
            kind = kind,
            base = config,
            gameDir = config.gameDir,
            ideName = kind.ideName,
        )
    }

    /**
     * Writes a run of [kind] named [runName], its args from [base] and its game
     * directory from [gameDir]. Returns the model so a variant can layer extras
     * on.
     */
    private fun writeRun(
        neoForge: NeoForgeExtension,
        runName: String,
        kind: RunKind,
        base: RunConfig,
        gameDir: Provider<Directory>,
        ideName: String,
    ): RunModel {
        val model = neoForge.runs.maybeCreate(runName)
        kind.applyRunType(model)

        model.gameDirectory.set(gameDir)
        model.jvmArguments.addAll(base.jvmArgs)
        model.programArguments.addAll(base.programArgs)
        model.systemProperties.putAll(base.systemProperties)
        model.environment.putAll(base.environment)
        model.devLogin.set(base.auth)
        model.ideName.set(ideName)

        return model
    }

    /**
     * Sets MDG's run type for this kind. GAMETEST has no factory method, so its
     * type is set directly.
     */
    private fun RunKind.applyRunType(model: RunModel) {
        when(this) {
            RunKind.CLIENT -> model.client()
            RunKind.SERVER -> model.server()
            RunKind.DATA -> model.data()
            RunKind.GAMETEST -> model.type.set("gameTestServer")
        }
    }

    /** The IntelliJ run-config name MDG surfaces for this kind. */
    private val RunKind.ideName: String
        get() = when(this) {
            RunKind.CLIENT -> "Minecraft Client"
            RunKind.SERVER -> "Minecraft Server"
            RunKind.DATA -> "Minecraft Data"
            RunKind.GAMETEST -> "Minecraft Game Test"
        }
}

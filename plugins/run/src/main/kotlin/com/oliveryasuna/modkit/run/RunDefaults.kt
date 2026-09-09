package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.run.extension.RunConfig

/** The run block's conventions. */
internal object RunDefaults : PluginFeature<RunContext> {

    override fun install(ctx: RunContext) {
        val run = ctx.run

        fixedRun(run.client, gameDir = "run/client", enabled = true)
        fixedRun(run.server, gameDir = "run/server", enabled = true)
        fixedRun(run.data, gameDir = "run/data", enabled = false)
        fixedRun(run.gametest, gameDir = "run/gametest", enabled = false)

        // It seems most developers working on mods prefer IntelliJ. This is
        // probably because most mod loader tutorials use IntelliJ. So, it's
        // probably a good idea to default to JetBrains' own runtime, rather
        // than expect developers to install DCEVM.
        run.hotswap.preferJetBrainsRuntime.convention(true)

        // Variant conventions apply as elements are registered.
        run.variants.all { variant ->
            variant.gameDir.convention("run/${variant.name}")
            variant.enabled.convention(true)
        }
    }

    private fun fixedRun(
        config: RunConfig,
        gameDir: String,
        enabled: Boolean
    ) {
        config.gameDir.convention(gameDir)
        config.enabled.convention(enabled)
        config.auth.convention(false)
    }

}

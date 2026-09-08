package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.plugin.PluginFeature

/**
 * Wires the run block onto whicehver loader base is present.
 *
 * This plugin never applies a base; it reacts to the one the loaders plugin
 * applied. Only one ever fires, so handling both backends the same context is
 * safe.
 */
internal object LoaderRuns : PluginFeature<RunContext> {

    private const val FABRIC_LOOM_ID: String = "fabric-loom"
    private const val NEOFORGE_MODDEV_ID: String = "net.neoforged.moddev"

    override fun install(ctx: RunContext) {
        val plugins = ctx.project.pluginManager
        plugins.withPlugin(FABRIC_LOOM_ID) { FabricRunBackend.configure(ctx) }
        // TODO: NeoForge backend.
    }

}

package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.plugin.PluginFeature

internal sealed interface LoaderBase : PluginFeature<LoaderContext> {

    companion object {

        /** The base that builds for [loader] */
        fun forLoader(loader: ModLoader): LoaderBase = when(loader) {
            ModLoader.FABRIC -> FabricBase
            ModLoader.NEOFORGE -> NeoForgeBase
        }

    }
}

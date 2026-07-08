package com.oliveryasuna.modkit.publish

import com.oliveryasuna.modkit.core.extension.McLoader

/**
 * Derives the publish compatibility (Minecraft versions + loader names) from
 * the Modkit model, scoped to the active loader. Pure and free of Gradle types
 * so it can be unit-tested directly.
 */
internal object PublishCompat {

    /** A model target flattened to the fields relevant to compatibility. */
    data class TargetView(
        val minecraftVersion: String,
        val enabled: Boolean,
        val loaders: Set<McLoader>
    )

    /**
     * The derived compatibility: distinct Minecraft versions and loader names.
     */
    data class Compat(
        val minecraftVersions: List<String>,
        val modLoaders: List<String>
    )

    /**
     * Minecraft versions are the distinct versions of enabled targets that
     * declare [activeLoader]; loaders are the single active loader's lowercased
     * name. When [activeLoader] is `null`, both are empty and publish is inert.
     */
    fun derive(targets: List<TargetView>, activeLoader: McLoader?): Compat {
        if(activeLoader == null) return Compat(emptyList(), emptyList())

        val minecraftVersions = targets
            .filter { it.enabled && activeLoader in it.loaders }
            .map { it.minecraftVersion }
            .distinct()

        return Compat(minecraftVersions, listOf(activeLoader.name.lowercase()))
    }

}

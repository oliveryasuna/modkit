package com.oliveryasuna.modkit.loaders.extension

/**
 * The base mapping namespace for a build. Parchment parameter names are layered
 * on top independently — controlled by whether [MappingsSpec.parchment] is set,
 * not by this enum — so there is no redundant "mojmap+parchment" value.
 */
public enum class MappingsScheme {

    /**
     * Mojang official mappings. The cross-loader common ground (NeoForge is
     * mojmap-native).
     */
    MOJMAP,

    /** Fabric community (Yarn) mappings. Fabric only. */
    YARN

}

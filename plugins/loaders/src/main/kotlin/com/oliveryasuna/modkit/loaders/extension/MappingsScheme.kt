package com.oliveryasuna.modkit.loaders.extension

/**
 * The base mapping namespace a build compiles against.
 *
 * Parchment parameter names layer on top of these independently, driven by
 * whether [MappingsSpec.parchment] has a version, so there is no separate
 * "mojmap plus parchment" entry to keep in sync.
 */
public enum class MappingsScheme {

    /**
     * Mojang's official mappings.\
     *
     * The cross-loader common ground. NeoForge is mojmap-native.
     */
    MOJMAP,

    /** Fabric's community (Yarn) mappings; Fabric only. */
    YARN

}

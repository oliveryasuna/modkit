package com.oliveryasuna.modkit.core.manifest

import org.gradle.api.provider.SetProperty

/**
 * Inter-plugin manifest contributions: generated data that sibling plugins
 * publish and `metadata` folds into the per-loader mod manifests, with no
 * cross-plugin implementation dependency.
 *
 * This is a shared, plugin-internal registry (distinct from the user-facing
 * `modkit { }` model) obtained via `plugin-support`'s
 * `Project.modkitManifestContributions()`. Extend it as new contributors appear
 * (e.g. access-widener / access-transformer references from `loaders`).
 */
public abstract class ModkitManifestContributions {

    /**
     * Mixin config file names (e.g. `"mymod.mixins.json"`), published by
     * `mixins`.
     */
    public abstract val mixinConfigs: SetProperty<String>

    /**
     * Fully-qualified `DataGeneratorEntrypoint` class names, published by
     * `datagen`. `metadata` folds them into fabric.mod.json
     * `entrypoints.fabric-datagen` (NeoForge datagen needs no manifest entry).
     */
    public abstract val fabricDatagenEntrypoints: SetProperty<String>

}

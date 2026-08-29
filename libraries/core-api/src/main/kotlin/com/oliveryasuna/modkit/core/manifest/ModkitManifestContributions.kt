package com.oliveryasuna.modkit.core.manifest

import org.gradle.api.provider.SetProperty

/**
 * A shared spot where one plugin drops manifest data for the `metadata` plugin
 * to pick up.
 *
 * This is how sibling plugins cooperate without knowing about each other. A
 * producer (the `mixins` plugin, say) writes here; the `metadata` plugin reads
 * here. One instance per project, handed out by `plugin-support`. Not part of
 * the user-facing `modkit { }` model.
 */
public abstract class ModkitManifestContributions {

    /**
     * Mixin config file names like "my_mod.mixins.json". Written by the
     * `mixins` plugin, folded into `fabric.mod.json` and `neoforge.mods.toml`.
     */
    public abstract val mixinConfigs: SetProperty<String>

    /**
     * Fully-qualified `DataGeneratorEntrypoint` class names
     */
    public abstract val fabricDatagenEntrypoints: SetProperty<String>

}

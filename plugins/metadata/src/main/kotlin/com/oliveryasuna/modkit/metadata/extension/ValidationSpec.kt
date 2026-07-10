package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.Property

public abstract class ValidationSpec {

    public abstract val failOnMissingIcon: Property<Boolean>
    public abstract val failOnInvalidSemver: Property<Boolean>
    public abstract val failOnUndeclaredMixinConfig: Property<Boolean>

    /**
     * Fail when the active loader is NeoForge and no `license` is set. NeoForge
     * requires a license in `neoforge.mods.toml`; omitting it produces a mod
     * the loader rejects at startup. Default `true`.
     */
    public abstract val failOnMissingLicense: Property<Boolean>

}

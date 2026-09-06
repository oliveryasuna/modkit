package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.Property

public abstract class ValidationSpec {

    /**
     * Fail when a declared icon file is nowhere in the common source set's
     * resources. Default `true`.
     */
    public abstract val failOnMissingIcon: Property<Boolean>

    /** Fail when the mod version is not valid semver. Default `true`. */
    public abstract val failOnInvalidSemver: Property<Boolean>

    /**
     * Fail on a referenced mixin config that was never declared.
     * Default `true`.
     */
    public abstract val failOnUndeclaredMixinConfig: Property<Boolean>

    /**
     * Fail when NeoForge is the active loader and no license is set. NeoForge
     * requires one in `neoforge.mods.toml`; without it, the loader rejects the
     * mod at startup. Default `true`.
     */
    public abstract val failOnMissingLicense: Property<Boolean>

}

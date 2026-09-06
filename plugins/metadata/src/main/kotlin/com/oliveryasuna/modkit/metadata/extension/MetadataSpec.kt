package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

/**
 * The `modkit { metadata { } }` block: the manifest-shaped extras that sit on
 * top of the core model's identity fields.
 *
 * Identity (id, version, authors, urls, ...) comes from the `modkit { }` model;
 * this adds the icon, the environment, entrypoints, declared dependencies, raw
 * per-loader overrides, and the validation switches.
 */
public abstract class MetadataSpec {

    /** Icon resource path, e.g., `"assets/mymod/icon.png"`. */
    public abstract val icon: Property<String>

    /** Which side the mod loads on: `"*"`, `"client"`, or `"server"`. */
    public abstract val environment: Property<String>

    /**
     * When `true`, expand `${version}` / `${modId}` / `${name}` / `${group}` /
     * `${minecraft}` placeholders in the generated manifest's string values
     * (raw overrides included). Off by default, so any `${...}` is emitted
     * literally; with it on, an unknown token fails the build.
     */
    public abstract val substituteTokens: Property<Boolean>

    @get:Nested
    public abstract val entrypoints: EntrypointsSpec

    @get:Nested
    public abstract val dependsOn: DependenciesSpec

    /** Raw keys merged into `fabric.mod.json`. */
    @get:Nested
    public abstract val fabric: RawOverrides

    /** Raw keys merged into `neoforge.mods.toml`. */
    @get:Nested
    public abstract val neoforge: RawOverrides

    @get:Nested
    public abstract val validation: ValidationSpec

    public fun entrypoints(action: Action<in EntrypointsSpec>): Unit = action.execute(entrypoints)

    public fun dependsOn(action: Action<in DependenciesSpec>): Unit = action.execute(dependsOn)

    public fun validation(action: Action<in ValidationSpec>): Unit = action.execute(validation)

}

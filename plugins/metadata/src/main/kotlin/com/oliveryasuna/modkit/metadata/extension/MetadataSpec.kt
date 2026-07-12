package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class MetadataSpec {

    public abstract val icon: Property<String>
    public abstract val environment: Property<String>

    /**
     * When `true`, expand `${version}`/`${modId}`/`${name}`/`${group}`/
     * `${minecraft}` placeholders in the generated manifest's string values
     * (including raw overrides). Off by default; an unknown token then fails
     * the build. Leave off to emit any `${...}` literally.
     */
    public abstract val substituteTokens: Property<Boolean>

    @get:Nested
    public abstract val entrypoints: EntrypointsSpec

    @get:Nested
    public abstract val dependsOn: DependenciesSpec

    @get:Nested
    public abstract val fabric: RawOverrides

    @get:Nested
    public abstract val neoforge: RawOverrides

    @get:Nested
    public abstract val validation: ValidationSpec

    public fun entrypoints(action: Action<in EntrypointsSpec>) {
        action.execute(entrypoints)
    }

    public fun dependsOn(action: Action<in DependenciesSpec>) {
        action.execute(dependsOn)
    }

    public fun validation(action: Action<in ValidationSpec>) {
        action.execute(validation)
    }

}

package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class MetadataSpec {

    public abstract val icon: Property<String>
    public abstract val environment: Property<String>

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

package com.oliveryasuna.modkit.multiversion.extension

import org.gradle.api.provider.MapProperty

/**
 * Overrides applied to the project when its node version matches an
 * `onVersion(range)` predicate.
 */
public abstract class VersionOverride {

    internal val properties: LinkedHashMap<String, String> = LinkedHashMap()

    /**
     * Extra dependencies for the matching versions, as `"group:name"` ->
     * version.
     */
    public abstract val dependencies: MapProperty<String, String>

    /**
     * Sets a project extra property, readable by the rest of the build script.
     */
    public fun property(key: String, value: String) {
        properties[key] = value
    }
}

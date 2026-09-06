package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.MapProperty

/**
 * Declared mod dependencies, keyed by mod id. Written into both manifests, as
 * `depends`/`recommends` on Fabric and `[[dependencies]]` on NeoForge.
 */
public abstract class DependenciesSpec {

    public abstract val constraints: MapProperty<String, DepConstraint>

    public fun required(
        id: String,
        range: String
    ): Unit =
        constraints.put(id, DepConstraint(range, DepConstraint.Kind.REQUIRED))

    public fun optional(
        id: String,
        range: String
    ): Unit =
        constraints.put(id, DepConstraint(range, DepConstraint.Kind.OPTIONAL))

}

package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.MapProperty

public abstract class DependenciesSpec {

    public abstract val constraints: MapProperty<String, DepConstraint>

    public fun required(id: String, range: String) {
        constraints.put(id, DepConstraint(range, DepConstraint.Kind.REQUIRED))
    }

    public fun optional(id: String, range: String) {
        constraints.put(id, DepConstraint(range, DepConstraint.Kind.OPTIONAL))
    }

}

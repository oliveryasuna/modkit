package com.oliveryasuna.modkit.metadata.extension

import java.io.Serializable

/**
 * A single declared dependency: a version [range] and whether it is
 * [Kind.REQUIRED] or [Kind.OPTIONAL]. Serializable so it can back a task input
 * under the configuration cache.
 */
public data class DepConstraint(
    public val range: String,
    public val kind: Kind
) : Serializable {

    public enum class Kind {

        REQUIRED,

        OPTIONAL

    }

}

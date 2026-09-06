package com.oliveryasuna.modkit.metadata.extension

import java.io.Serializable

public data class DepConstraint(
    public val range: String,
    public val kind: Kind
) : Serializable {

    public enum class Kind {

        REQUIRED,

        OPTIONAL

    }

}

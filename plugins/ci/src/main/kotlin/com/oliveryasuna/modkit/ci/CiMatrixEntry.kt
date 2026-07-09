package com.oliveryasuna.modkit.ci

import java.io.Serializable

/**
 * A single version x loader cell of the build matrix. Serializable so it can be
 * carried as a task input across the configuration cache.
 */
internal data class CiMatrixEntry(
    val minecraftVersion: String,
    val loader: String
) : Serializable {

    private companion object {

        private const val serialVersionUID: Long = 1L

    }

}

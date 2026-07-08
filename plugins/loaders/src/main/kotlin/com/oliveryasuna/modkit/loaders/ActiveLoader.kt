package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import org.gradle.api.GradleException

/**
 * Resolves the active loader from the `modkit.loader` Gradle property. The base
 * (Fabric Loom vs ModDevGradle) must be chosen eagerly in `apply()`, before the
 * `modkit` model DSL runs, so this reads a property rather than the model.
 */
internal object ActiveLoader {

    /**
     * Maps a raw `modkit.loader` value to its [McLoader], or `null` when
     * unset/blank. Delegates to the shared contract and surfaces bad values as
     * a Gradle-friendly build failure.
     */
    fun resolve(raw: String?): McLoader? =
        try {
            McLoader.fromProperty(raw)
        } catch(e: IllegalArgumentException) {
            throw GradleException(e.message ?: "Invalid '${McLoader.PROPERTY}' value", e)
        }

}

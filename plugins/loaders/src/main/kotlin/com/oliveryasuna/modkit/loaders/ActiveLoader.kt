package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import org.gradle.api.GradleException

/**
 * Resolves the active loader from the `modkit.loader` Gradle property. The base
 * (Fabric Loom vs ModDevGradle) must be chosen eagerly in `apply()`, before the
 * `modkit` model DSL runs, so this reads a property rather than the model.
 */
internal object ActiveLoader {

    const val PROPERTY: String = "modkit.loader"

    /** Loaders that currently have a base wired (Loom / MDG). */
    private val SUPPORTED: Set<McLoader> = setOf(McLoader.FABRIC, McLoader.NEOFORGE)

    /**
     * Maps a raw `modkit.loader` value to a supported [McLoader], or `null`
     * when unset/blank. Throws on unknown or unsupported values so typos fail
     * fast.
     */
    fun resolve(raw: String?): McLoader? {
        val value = raw?.trim()
        if(value.isNullOrEmpty()) return null

        val loader = McLoader.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                     ?: throw GradleException("Unknown '$PROPERTY' value '$value'; expected one of " + McLoader.entries.joinToString { it.name.lowercase() })

        return loader
    }

}

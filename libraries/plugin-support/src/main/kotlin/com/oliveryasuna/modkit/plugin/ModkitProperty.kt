package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.plugin.ModkitProperties.COMMON_SOURCE_SET
import org.gradle.api.GradleException
import org.gradle.api.Project


/**
 * The `modkit.*` Gradle properties the build knows about, gathered in one
 * place.
 *
 * These live as properties instead of model values on purpose. They settle
 * structural questions a pluigin has to answer early, up in `plugins { }`
 * before the `modkit { } ` DSL has to run, so the lazy model is no help yet.
 */
public object ModkitProperties {

    /** Selects the active loader. The same string as [ModLoader.PROPERTY]. */
    public const val LOADER: String = ModLoader.PROPERTY

    /** Names the mod's common (shared) source set. */
    public const val COMMON_SOURCE_SET: String = "${MODKIT_PROPERTY_PREFIX}.commonSourceSet"

    /** Common source set used when [COMMON_SOURCE_SET] is left unset. */
    public const val DEFAULT_COMMON_SOURCE_SET: String = "main"

}

/**
 * The loader this project is building for, or `null` when none is set.
 *
 * A value that names no known loader throws, so a typo stops the build instead
 * of quietly compiling nothing.
 */
public fun Project.activeLoader(): ModLoader? =
    try {
        ModLoader.fromProperty(modkitProperty(ModkitProperties.LOADER))
    } catch(cause: IllegalArgumentException) {
        throw GradleException(cause.message ?: "Invalid '${ModkitProperties.LOADER}' value.", cause)
    }

/**
 * The mod's common source set: whatever [ModkitProperties.COMMON_SOURCE_SET]
 * points at, or "main" if it points at nothing.
 */
public fun Project.commonSourceSet(): String =
    modkitProperty(COMMON_SOURCE_SET)
        ?: ModkitProperties.DEFAULT_COMMON_SOURCE_SET

/**
 * Reads a `modkit.*` property, trimmed, returning `null` when it is absent or
 * blank.
 *
 * [Project.findProperty] on purpose, not `providers.gradleProperty`. The
 * `multiversion` plugin publishes some of these per Stonecutter node as extra
 * properties from a Settings `beforeProject` hook, and only findProperty sees
 * those; a plain `-P` or `gradle.properties` value flows through the same path.
 * It is read at configuration time and never held by a task, so it stays
 * configuration-cache safe.
 */
private fun Project.modkitProperty(name: String): String? =
    findProperty(name)?.toString()?.trim()?.takeIf(String::isNotEmpty)

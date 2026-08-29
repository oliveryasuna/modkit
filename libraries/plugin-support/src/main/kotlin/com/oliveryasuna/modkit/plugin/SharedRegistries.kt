package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.diagnostics.ModkitDiagnostics
import com.oliveryasuna.modkit.core.manifest.ModkitManifestContributions
import org.gradle.api.Project

/** Extension name for the manifest-contributions registry. */
private const val MANIFEST_CONTRIBUTIONS_NAME: String = "${MODKIT_EXTENSION_PREFIX}ManifestContributions"

/** Extension name for the diagnostics registry. */
private const val DIAGNOSTICS_NAME: String = "${MODKIT_EXTENSION_PREFIX}Diagnostics"

/**
 * The shared registry sibling plugins write manifest data into.
 *
 * One per project: created the first time someone asks for it, reused every
 * time after. Any modkit plugin can call this safely, in any order.
 */
public fun Project.modkitManifestContributions(): ModkitManifestContributions =
    getOrCreateExtension(MANIFEST_CONTRIBUTIONS_NAME)

/**
 * The shared registry sibling plugins write `modkitDoctor` sections and problems
 * into.
 *
 * Same one-per-project, get-or-create story as [modkitManifestContributions].
 */
public fun Project.modkitDiagnostics(): ModkitDiagnostics =
    getOrCreateExtension(DIAGNOSTICS_NAME)

/**
 * Looks up a project extension of type [T], creating it under [name] on first
 * use.
 *
 * This is the bit both registries share. Keeping it in one place is what lets
 * them be get-or-create without two plugins racing to create the same thing
 * twice.
 */
internal inline fun <reified T : Any> Project.getOrCreateExtension(name: String): T =
    extensions.findByType(T::class.java)
    ?: extensions.create(name, T::class.java)

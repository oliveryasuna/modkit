package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.metadata.extension.DepConstraint

/**
 * Plain, provider-free snapshot of everything a manifest builder needs.
 * Resolved from the model/spec at task execution time so the builders stay
 * pure and unit-testable without live Gradle providers.
 */
internal data class ManifestInputs(
    val modId: String,
    val version: String,
    val displayName: String,
    val description: String?,
    val authors: List<String>,
    val license: String?,
    val icon: String?,
    val homepage: String?,
    val source: String?,
    val issues: String?,
    val environment: String,
    val minecraftVersion: String?,
    val entrypointsMain: List<String>,
    val entrypointsClient: List<String>,
    val dependencies: Map<String, DepConstraint>,
    val rawOverrides: Map<String, Any>
)

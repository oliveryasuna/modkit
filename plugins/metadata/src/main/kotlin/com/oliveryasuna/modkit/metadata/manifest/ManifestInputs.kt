package com.oliveryasuna.modkit.metadata.manifest

import com.oliveryasuna.modkit.metadata.extension.DepConstraint

/**
 * A flat, provider-free snapshot of everything a manifest builder needs.
 *
 * The generate task resolves this from the model at execution time, so the
 * builders never touch a live Gradle provider and can be tested with plain
 * data.
 */
internal data class ManifestInputs(
    val modId: String,
    val version: String,
    val group: String,
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
    val mixinConfigs: List<String>,
    val fabricDatagenEntrypoints: List<String>,
    val rawOverrides: Map<String, Any>,
    val substituteTokens: Boolean,
)

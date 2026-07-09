package com.oliveryasuna.modkit.ci

/**
 * Plain, provider-free snapshot of everything the workflow builder needs.
 * Resolved from the model/spec at task execution time so the builder stays pure
 * and unit-testable without live Gradle providers.
 */
internal data class CiWorkflowInputs(
    val matrix: List<CiMatrixEntry>,
    val java: Int,
    val cache: Boolean,
    val publishOnTag: Boolean,
    val publishApplied: Boolean,
    val modrinthSecret: String,
    val curseforgeSecret: String,
    val githubSecret: String
)

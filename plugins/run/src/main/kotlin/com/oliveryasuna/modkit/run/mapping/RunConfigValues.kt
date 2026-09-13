package com.oliveryasuna.modkit.run.mapping

import com.oliveryasuna.modkit.run.extension.RunConfig

/**
 * A plain, provider-free snapshot of a [RunConfig].
 *
 * Pulling the resolved values out of Gradle's lazy types keeps the mapping
 * logic pure, so it can be unit-tested without applying Loom or MDG.
 */
internal data class RunConfigValues(
    val jvmArgs: List<String>,
    val programArgs: List<String>,
    val systemProperties: Map<String, String>,
    val environment: Map<String, String>,
    val auth: Boolean,
    val enabled: Boolean,
)

/** Resolves a [RunConfig]'s providers into a snapshot. */
internal fun RunConfig.snapshot(): RunConfigValues =
    RunConfigValues(
        jvmArgs = jvmArgs.getOrElse(emptyList()),
        programArgs = programArgs.getOrElse(emptyList()),
        systemProperties = systemProperties.getOrElse(emptyMap()),
        environment = environment.getOrElse(emptyMap()),
        auth = auth.getOrElse(false),
        enabled = enabled.getOrElse(false),
    )

/**
 * Derives a variant's run values from this base run's: forced enabled, list
 * args appended, and map args merged over the base's (the variant wins on a key
 * collision). The variant's game directory is applied separately, straight from
 * its [org.gradle.api.file.DirectoryProperty].
 */
internal fun RunConfigValues.mergeVariant(
    jvmArgs: List<String>,
    programArgs: List<String>,
    systemProperties: Map<String, String>,
    environment: Map<String, String>,
): RunConfigValues =
    copy(
        enabled = true,
        jvmArgs = this.jvmArgs + jvmArgs,
        programArgs = this.programArgs + programArgs,
        systemProperties = this.systemProperties + systemProperties,
        environment = this.environment + environment,
    )

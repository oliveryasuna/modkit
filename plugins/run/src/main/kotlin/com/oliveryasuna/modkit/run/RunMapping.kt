package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig

/**
 * A plain, provider-free snapshot of a [RunConfig]. Isolating the resolved
 * values from Gradle's lazy types keeps the mapping logic pure and unit
 * testable without applying Loom or ModDevGradle.
 */
internal data class RunConfigValues(
    val gameDir: String,
    val jvmArgs: List<String>,
    val programArgs: List<String>,
    val systemProperties: Map<String, String>,
    val environment: Map<String, String>,
    val auth: Boolean,
    val enabled: Boolean
)

/**
 * Derives a variant's run values from this base run's: the variant gets its own
 * game directory, is forced enabled, appends its extra list args, and merges
 * its map args over the base's (the variant wins on a key collision).
 */
internal fun RunConfigValues.mergeVariant(
    gameDir: String,
    jvmArgs: List<String>,
    programArgs: List<String>,
    systemProperties: Map<String, String>,
    environment: Map<String, String>
): RunConfigValues =
    copy(
        gameDir = gameDir,
        enabled = true,
        jvmArgs = this.jvmArgs + jvmArgs,
        programArgs = this.programArgs + programArgs,
        systemProperties = this.systemProperties + systemProperties,
        environment = this.environment + environment
    )

/** Resolves a [RunConfig]'s providers into a [RunConfigValues] snapshot. */
internal fun RunConfig.snapshot(): RunConfigValues =
    RunConfigValues(
        gameDir = gameDir.get(),
        jvmArgs = jvmArgs.getOrElse(emptyList()),
        programArgs = programArgs.getOrElse(emptyList()),
        systemProperties = systemProperties.getOrElse(emptyMap()),
        environment = environment.getOrElse(emptyMap()),
        auth = auth.getOrElse(false),
        enabled = enabled.getOrElse(false)
    )

/** Maps system properties to `-Dkey=value` JVM arguments. */
internal fun systemPropertiesToVmArgs(properties: Map<String, String>): List<String> =
    properties.map { (key, value) -> "-D$key=$value" }

/**
 * The concrete arguments a Fabric Loom run should receive, plus warnings for
 * unified features Loom cannot express (which are dropped rather than failing).
 */
internal data class LoomRunMapping(
    val runDir: String,
    val vmArgs: List<String>,
    val programArgs: List<String>,
    val warnings: List<String>
)

/**
 * Pure mapping from a unified run config to Loom's model. Loom has no system
 * property map (properties become `-D` JVM arguments), no environment variable
 * support, and no dev-login helper — the latter two are reported as warnings.
 */
internal fun mapRunConfigToLoom(name: String, values: RunConfigValues): LoomRunMapping {
    val vmArgs = values.jvmArgs + systemPropertiesToVmArgs(values.systemProperties)

    val warnings = buildList {
        if(values.environment.isNotEmpty()) {
            add(
                "modkit.run: environment variables on '$name' are not supported on Fabric " +
                "(Loom has no run environment-variable support); skipping ${values.environment.keys}."
            )
        }
        if(values.auth) {
            add(
                "modkit.run: 'auth' (dev login) on '$name' is not supported on Fabric " +
                "(no built-in dev login); skipping."
            )
        }
    }

    return LoomRunMapping(
        runDir = values.gameDir,
        vmArgs = vmArgs,
        programArgs = values.programArgs,
        warnings = warnings
    )
}

/**
 * Builds the human-readable hot-swap guidance printed by `modkitRunInfo`,
 * given the running JVM's vendor and VM name. Kept pure so it can be tested
 * without launching a specific JVM.
 */
internal fun hotswapReport(vendor: String, vmName: String, preferJetBrainsRuntime: Boolean): String {
    val isJetBrainsRuntime =
        vmName.contains("JetBrains", ignoreCase = true) || vendor.contains("JetBrains", ignoreCase = true)
    val isDcevm =
        vmName.contains("Dynamic Code Evolution", ignoreCase = true) || vmName.contains("DCEVM", ignoreCase = true)

    val status = when {
        isJetBrainsRuntime -> "JetBrains Runtime detected — enhanced class redefinition is available."
        isDcevm -> "DCEVM detected — enhanced class redefinition is available."
        else -> "Standard HotSpot JVM detected — only method-body hot-swap is available."
    }

    val guidance = when {
        !preferJetBrainsRuntime ->
            "hotswap.preferJetBrainsRuntime is disabled; no enhanced hot-swap is requested."

        isJetBrainsRuntime || isDcevm ->
            "hotswap.preferJetBrainsRuntime is enabled and satisfied by the current JVM."

        else ->
            "hotswap.preferJetBrainsRuntime is enabled but the current JVM lacks enhanced hot-swap. " +
            "Run Gradle on a JetBrains Runtime (or a DCEVM-enabled JVM) to add/remove methods and fields at runtime."
    }

    return "$status $guidance"
}

package com.oliveryasuna.modkit.run.mapping

/**
 * The concrete arguments a Fabric Loom run should get, plus warnings for
 * anything the unified config asked for that Loom cannot express (dropped, not
 * fatal).
 */
internal data class LoomRunMapping(
    val vmArgs: List<String>,
    val programArgs: List<String>,
    val warnings: List<String>,
)

/**
 * Pure mapping from a unified run config onto Loom's model.
 *
 * Loom has no system-property map (properties become `-D` JVM args), no
 * environment variables, and no dev-login helper. The last two have no
 * equivalent, so they come back as warnings.
 */
internal fun mapRunConfigToLoom(
    name: String,
    values: RunConfigValues
): LoomRunMapping {
    val vmArgs = values.jvmArgs + values.systemProperties.toVmArgs()

    val warnings = buildList {
        if(values.environment.isNotEmpty()) {
            add(
                "modkit.run: environment variables on '$name' are not supported on Fabric " +
                        "(Loom has no run environment-variable support); skipping ${values.environment.keys}.",
            )
        }
        if(values.auth) {
            add(
                "modkit.run: 'auth' (dev login) on '$name' is not supported on Fabric (no built-in dev login); " +
                        "skipping."
            )
        }
    }

    return LoomRunMapping(
        vmArgs = vmArgs,
        programArgs = values.programArgs,
        warnings = warnings,
    )
}

/** Turns system properties into `-Dkey=value` JVM arguments. */
internal fun Map<String, String>.toVmArgs(): List<String> =
    map { (key, value) -> "-D$key=$value" }

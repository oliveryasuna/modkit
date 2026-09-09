package com.oliveryasuna.modkit.run.mapping

/**
 * Builds the hot-swap guidance `modkitRunInfo` prints, from the running JVM's
 * vendor and VM name.
 *
 * Pure, so it can be tested without launching a given JVM.
 */
internal fun hotswapReport(vendor: String, vmName: String, preferJetBrainsRuntime: Boolean): String {
    val isJetBrainsRuntime = vmName.containsIgnoreCase("JetBrains") || vendor.containsIgnoreCase("JetBrains")
    val isDcevm = vmName.containsIgnoreCase("Dynamic Code Evolution") || vmName.containsIgnoreCase("DCEVM")
    val enhanced = isJetBrainsRuntime || isDcevm

    val status = when {
        isJetBrainsRuntime -> "JetBrains Runtime detected. Enhanced class redefinition is available."
        isDcevm -> "DCEVM detected. Enhanced class redefinition is available."
        else -> "Standard HotSpot JVM detected. Only method-body hot-swap is available."
    }

    val guidance = when {
        !preferJetBrainsRuntime -> "hotswap.preferJetBrainsRuntime is disabled; no enhanced hot-swap is requested."
        enhanced -> "hotswap.preferJetBrainsRuntime is enabled and satisfied by the current JVM."
        else ->
            "hotswap.preferJetBrainsRuntime is enabled but the current JVM lacks enhanced hot-swap. " +
                    "Run Gradle on a JetBrains Runtime (or a DCEVM-enabled JVM) to add and remove methods and fields at " +
                    "runtime."
    }

    return "$status $guidance"
}

private fun String.containsIgnoreCase(other: String): Boolean = contains(other, ignoreCase = true)

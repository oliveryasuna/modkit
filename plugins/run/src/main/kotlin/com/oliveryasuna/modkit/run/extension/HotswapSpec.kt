package com.oliveryasuna.modkit.run.extension

import org.gradle.api.provider.Property

/**
 * Advisory hot-swap preferences. In this version nothing is injected into the
 * JVM; `modkitRunInfo` probes the running JVM and reports whether an enhanced
 * hot-swap runtime (JetBrains Runtime / DCEVM) is available.
 */
public abstract class HotswapSpec {

    /**
     * Whether a JetBrains Runtime is preferred for enhanced class redefinition.
     * Advisory only — this does not inject any JVM argument.
     */
    public abstract val preferJetBrainsRuntime: Property<Boolean>

}

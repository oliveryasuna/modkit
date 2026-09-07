package com.oliveryasuna.modkit.run.extension

import org.gradle.api.provider.Property

/**
 * Advisory hot-swap preferences.
 *
 * Nothing here is injected into any JVM. `modkitRunInfo` probes the running JVM
 * and reports whether enhanced class redefinition (JetBrains Runtime / DCEVM)
 * is available; acting on that is left to the user.
 */
public abstract class HotswapSpec {

    /**
     * Prefer a JetBrains Runtime for enhanced class redefinition. Advisory
     * only.
     */
    public abstract val preferJetBrainsRuntime: Property<Boolean>

}

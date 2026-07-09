package com.oliveryasuna.modkit.run.extension

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * A single unified run configuration. This is a superset of what any one loader
 * supports; features the active loader cannot express are logged and skipped
 * rather than failing the build.
 */
public abstract class RunConfig {

    /** Working directory for the run, relative to the project. */
    public abstract val gameDir: Property<String>

    /** Extra JVM arguments. */
    public abstract val jvmArgs: ListProperty<String>

    /** Extra program (game) arguments. */
    public abstract val programArgs: ListProperty<String>

    /**
     * System properties. On loaders without a native map (Fabric Loom), these
     * are emulated by appending `-Dkey=value` to the JVM arguments.
     */
    public abstract val systemProperties: MapProperty<String, String>

    /**
     * Environment variables. Unsupported on Fabric Loom (logged and skipped).
     */
    public abstract val environment: MapProperty<String, String>

    /**
     * Whether to enable a real-account dev login for this run. Unsupported on
     * Fabric Loom (logged and skipped).
     */
    public abstract val auth: Property<Boolean>

    /** Whether this run is configured on the active loader. */
    public abstract val enabled: Property<Boolean>

}

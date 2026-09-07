package com.oliveryasuna.modkit.run.extension

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * One unified run configuration.
 *
 * It is a superset of what any single loader can do; anything the active loader
 * cannot express is logged and skipped, rather than failing the build.
 */
public abstract class RunConfig {

    /** Working directory for the run, relative to the project. */
    public abstract val gameDir: Property<String>

    /** Extra JVM arguments. */
    public abstract val jvmArgs: ListProperty<String>

    /** Extra program (game) arguments. */
    public abstract val programArgs: ListProperty<String>

    /**
     * System properties.
     */
    public abstract val systemProperties: MapProperty<String, String>

    /**
     * Environment variables. Not supported on Fabric Loom (logged and skipped).
     */
    public abstract val environment: MapProperty<String, String>

    /**
     * Real-account dev login for this run. Not supported on Fabric Loom (logged
     * and skipped).
     */
    public abstract val auth: Property<Boolean>

    /** Whether this run is configured on the active loader. */
    public abstract val enabled: Property<Boolean>

}

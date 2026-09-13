package com.oliveryasuna.modkit.run.extension

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * One unified run configuration.
 *
 * It is a superset of what any single loader can do; anything the active loader
 * cannot express is logged and skipped, rather than failing the build.
 */
public abstract class RunConfig @Inject constructor(
    private val layout: ProjectLayout
) {

    /** Working directory for the run. Defaults under `run/`, in the project. */
    public abstract val gameDir: DirectoryProperty

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

    /**
     * Sets [gameDir] from a path resolved against the project directory, e.g.,
     * `gameDir("run/dev")`.
     */
    public fun gameDir(path: String): Unit = gameDir.set(layout.projectDirectory.dir(path))

}

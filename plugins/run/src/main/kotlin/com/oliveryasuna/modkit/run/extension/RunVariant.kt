package com.oliveryasuna.modkit.run.extension

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.*
import javax.inject.Inject

/**
 * A compatibility-test run variant: a parallel set of runs that launch with
 * extra mod jars staged into an isolated game directory. Use it to test your
 * mod against other mods without touching the main run.
 *
 * For each run kind named by [appliesTo], the variant clones that run (same
 * JVM/program args, its own `gameDir`) and adds a staging step that copies
 * [mods] into `<gameDir>/mods/` before launch.
 */
public abstract class RunVariant @Inject constructor(
    private val variantName: String,
    private val siblings: NamedDomainObjectContainer<RunVariant>,
    private val providers: ProviderFactory,
    private val layout: ProjectLayout
) : Named {

    /** Working directory for this variant's runs. Default `run/<name>`. */
    public abstract val gameDir: DirectoryProperty

    /** Whether this variant's runs are configured. Default `true`. */
    public abstract val enabled: Property<Boolean>

    /** Mod-jar coordinates staged into `<gameDir>/mods/` before launch. */
    public abstract val modCoordinates: ListProperty<String>

    /** Run kinds (`client`/`server`/`data`/`gametest`) this variant clones. */
    public abstract val appliesToRuns: SetProperty<String>

    /** Extra JVM arguments, **appended** to the cloned base run's. */
    public abstract val jvmArgs: ListProperty<String>

    /** Extra program (game) arguments, **appended** to the cloned base run's. */
    public abstract val programArgs: ListProperty<String>

    /**
     *  Extra system properties, **merged over** the base run's (the variant
     *  wins on a clash).
     */
    public abstract val systemProperties: MapProperty<String, String>

    /**
     * Extra environment variables, **merged over** the base run's (the variant
     * wins).
     */
    public abstract val environment: MapProperty<String, String>

    /**
     * Adds mod-jar coordinate strings to stage, e.g.,
     * `"maven.modrinth:modmenu:..."`.
     */
    public fun mods(vararg coordinates: String): Unit = modCoordinates.addAll(*coordinates)

    /**
     * Version-catalog overload of [mods]. Each dependency is appended lazily,
     * so catalog resolution waits until the staging configuration resolves.
     */
    public fun mods(vararg deps: Provider<MinimalExternalModuleDependency>) {
        deps.forEach { dependency ->
            modCoordinates.add(
                dependency.map { "${it.module.group}:${it.module.name}:${it.versionConstraint.requiredVersion}" },
            )
        }
    }

    /**
     * Inherits each named variant's mods into this one, lazily. The named
     * variants only have to exist by the time the staging configuration
     * resolves, so the order of `register(...)` calls does not matter. Looking
     * a sibling up eagerly (`container.named(name)`) would instead throw here
     * if it is registered later.
     */
    public fun extends(vararg names: String) {
        names.forEach { name ->
            modCoordinates.addAll(providers.provider { siblings.getByName(name).modCoordinates.get() })
        }
    }

    /**
     * Names the run kinds this variant clones
     * (`client`/`server`/`data`/`gametest`).
     */
    public fun appliesTo(vararg runKinds: String): Unit = appliesToRuns.addAll(*runKinds)

    /**
     * Sets [gameDir] from a path resolved against the project directory, e.g.,
     * `gameDir("run/compat")`.
     */
    public fun gameDir(path: String): Unit = gameDir.set(layout.projectDirectory.dir(path))

    override fun getName(): String = variantName
}

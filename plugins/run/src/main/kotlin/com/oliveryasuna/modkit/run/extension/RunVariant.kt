package com.oliveryasuna.modkit.run.extension

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.*
import javax.inject.Inject

/**
 * A compatibility-test run variant: a parallel set of runs that launch with a
 * defined set of extra mod jars staged into an isolated game directory. Use it
 * to test your mod against other mods without polluting the main run.
 *
 * For each run kind named by [appliesTo], the variant produces a clone of that
 * run (same JVM/program args, its own `gameDir`) plus a staging step that
 * copies [mods] into `<gameDir>/mods/` before launch.
 */
public abstract class RunVariant @Inject constructor(
    private val variantName: String,
    private val siblings: NamedDomainObjectContainer<RunVariant>,
    private val providers: ProviderFactory
) : Named {

    /** Working directory for this variant's runs. Default `run/<name>`. */
    public abstract val gameDir: Property<String>

    /** Whether this variant's runs are configured. Default `true`. */
    public abstract val enabled: Property<Boolean>

    /** Mod-jar coordinates staged into `<gameDir>/mods/` before launch. */
    public abstract val modCoordinates: ListProperty<String>

    /** Names of run kinds (`client`/`server`/`data`/`gametest`) to clone. */
    public abstract val appliesToRuns: SetProperty<String>

    /**
     * Adds extra mod-jar coordinate strings to stage (e.g.
     * `maven.modrinth:modmenu:...`).
     */
    public fun mods(vararg coordinates: String) {
        modCoordinates.addAll(*coordinates)
    }

    /**
     * Version-catalog overload of [mods]. Each dependency is appended lazily so
     * catalog resolution is deferred until the staging configuration resolves.
     */
    public fun mods(vararg deps: Provider<MinimalExternalModuleDependency>) {
        deps.forEach { provider ->
            modCoordinates.add(
                provider.map { dep ->
                    "${dep.module.group}:${dep.module.name}:${dep.versionConstraint.requiredVersion}"
                }
            )
        }
    }

    /**
     * Inherits each named variant's mods into this one — lazily, so the order
     * of `register(...)` calls does not matter (the named variants only have to
     * exist by the time the staging configuration resolves).
     */
    public fun extends(vararg names: String) {
        names.forEach { name ->
            // Look the sibling up inside a deferred provider so the named
            // variant only has to exist by the time the coordinates are
            // realized — `container.named(name)` would instead throw right now
            // if `name` is registered later than this variant.
            modCoordinates.addAll(providers.provider { siblings.getByName(name).modCoordinates.get() })
        }
    }

    /**
     * Names the run kinds this variant clones
     * (`client`/`server`/`data`/`gametest`).
     */
    public fun appliesTo(vararg runKinds: String) {
        appliesToRuns.addAll(*runKinds)
    }

    override fun getName(): String = variantName

}

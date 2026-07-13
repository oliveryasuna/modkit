package com.oliveryasuna.modkit.run.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

/**
 * The `modkit.run { }` block. Declares a unified, cross-loader set of run
 * configurations that map onto Fabric Loom run configs and ModDevGradle runs.
 */
public abstract class RunSpec @Inject constructor(objects: ObjectFactory) {

    @get:Nested
    public abstract val client: RunConfig

    @get:Nested
    public abstract val server: RunConfig

    @get:Nested
    public abstract val data: RunConfig

    @get:Nested
    public abstract val gametest: RunConfig

    @get:Nested
    public abstract val hotswap: HotswapSpec

    /**
     * Compatibility-test variants — parallel runs that stage extra mods into an
     * isolated game directory. The factory threads the container back to each
     * element so `extends("...")` can look up its siblings; the `lateinit`
     * closes the cycle (the factory only fires on `register(...)`, after the
     * container is assigned).
     */
    public val variants: NamedDomainObjectContainer<RunVariant> = run {
        lateinit var container: NamedDomainObjectContainer<RunVariant>
        container = objects.domainObjectContainer(RunVariant::class.java) { name ->
            objects.newInstance(RunVariant::class.java, name, container)
        }
        container
    }

    public fun variants(action: Action<in NamedDomainObjectContainer<RunVariant>>) {
        action.execute(variants)
    }

    public fun client(action: Action<in RunConfig>) {
        action.execute(client)
    }

    public fun server(action: Action<in RunConfig>) {
        action.execute(server)
    }

    public fun data(action: Action<in RunConfig>) {
        action.execute(data)
    }

    public fun gametest(action: Action<in RunConfig>) {
        action.execute(gametest)
    }

    public fun hotswap(action: Action<in HotswapSpec>) {
        action.execute(hotswap)
    }

}

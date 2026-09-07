package com.oliveryasuna.modkit.run.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

/**
 * The `modkit { run { } }` block; one cross-loader set of run configurations
 * that maps onto Fabric Loom run configs and ModDevGradle runs.
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
     * Compatibility-test variants: parallel runs that stage extra mods into an
     * isolated game directory.
     */
    public val variants: NamedDomainObjectContainer<RunVariant> = variantContainer(objects)

    public fun client(action: Action<in RunConfig>): Unit = action.execute(client)

    public fun server(action: Action<in RunConfig>): Unit = action.execute(server)

    public fun data(action: Action<in RunConfig>): Unit = action.execute(data)

    public fun gametest(action: Action<in RunConfig>): Unit = action.execute(gametest)

    public fun hotswap(action: Action<in HotswapSpec>): Unit = action.execute(hotswap)

    public fun variants(action: Action<in NamedDomainObjectContainer<RunVariant>>): Unit = action.execute(variants)

}

private fun variantContainer(objects: ObjectFactory): NamedDomainObjectContainer<RunVariant> {
    lateinit var container: NamedDomainObjectContainer<RunVariant>
    container = objects.domainObjectContainer(RunVariant::class.java) { name ->
        objects.newInstance(RunVariant::class.java, name, container)
    }
    return container
}

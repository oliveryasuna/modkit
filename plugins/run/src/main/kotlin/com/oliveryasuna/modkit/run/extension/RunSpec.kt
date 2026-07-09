package com.oliveryasuna.modkit.run.extension

import org.gradle.api.Action
import org.gradle.api.tasks.Nested

/**
 * The `modkit.run { }` block. Declares a unified, cross-loader set of run
 * configurations that map onto Fabric Loom run configs and ModDevGradle runs.
 */
public abstract class RunSpec {

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

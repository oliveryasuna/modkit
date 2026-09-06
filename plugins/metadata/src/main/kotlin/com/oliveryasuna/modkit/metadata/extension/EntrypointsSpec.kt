package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.ListProperty

/**
 * Fabric entrypoints, by environment.
 *
 * N/A for NeoForge, since it discovers `@Mod`.
 */
public abstract class EntrypointsSpec {

    /** Common (both-sides) entrypoint class names. */
    public abstract val main: ListProperty<String>

    /** Client-only entrypoint class names. */
    public abstract val client: ListProperty<String>

    /** Adds one or more [main] entrypoints. */
    public fun main(vararg entrypoints: String): Unit = main.addAll(*entrypoints)

    /** Adds one or more [client] entrypoints. */
    public fun client(vararg entrypoints: String): Unit = client.addAll(*entrypoints)

}

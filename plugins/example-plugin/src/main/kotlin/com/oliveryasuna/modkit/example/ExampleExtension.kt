package com.oliveryasuna.modkit.example

import org.gradle.api.provider.Property

/**
 * Configuration for [ExamplePlugin].
 *
 * ```
 * example {
 *     message = "Custom greeting"
 * }
 * ```
 */
public interface ExampleExtension {

    /** Message printed by the `greet` task. */
    public val message: Property<String>
}

package com.oliveryasuna.modkit.testing.extension

import org.gradle.api.provider.Property

/**
 * The `modkit { testing { } }` block.
 *
 * Applying the `testing` plugin sets up the JUnit Platform on the `test` task
 * for pure-logic tests (no Minecraft classpath needed) — that is unconditional,
 * so the only knob here is GameTest.
 */
public abstract class TestingSpec {

    /**
     * Enables the loader's server-side GameTest run. On NeoForge this
     * configures a run named `gametest` of type `gameTestServer` (task
     * `runGametest`); on Fabric it enables Loom's server game tests via
     * `fabricApi.configureTests` (the `gametest` run). Client game tests
     * (Fabric-only, and EULA-gated) are not exposed by this single boolean.
     * Default `false`.
     */
    public abstract val gametest: Property<Boolean>

}

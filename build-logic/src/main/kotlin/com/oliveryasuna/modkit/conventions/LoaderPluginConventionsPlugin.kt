package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention for plugin modules that wrap loader tooling (Fabric Loom /
 * ModDevGradle) — `id("modkit.loader-plugin-conventions")`.
 *
 * Identical to [PluginConventionsPlugin] but raises the bytecode target to the
 * loader tooling's JVM floor (Java 21): a Java 17 target cannot link Loom/MDG,
 * which are compiled for 21. Consumers building mods through these plugins run
 * Gradle on Java 21+ regardless — that is the loader tooling's own floor, not a
 * choice Modkit imposes.
 *
 * The repo-wide Java 17 floor (see [BaseConventionsPlugin]) still applies to
 * the pure model/library modules that do not wrap loader tooling.
 */
class LoaderPluginConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        // 1. Set bytecode target.
        //
        // Set before applying the base convention so it reads the raised
        // target.
        //

        extensions.extraProperties.set("modkit.bytecodeTarget", "21")

        // 2. Apply plugins.

        plugins.apply(PluginConventionsPlugin::class.java)
    }
}

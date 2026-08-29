package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.extra

/**
 * [PluginConventionsPlugin] with the bytecode target raised to Java 21
 * (`modkit.loader-plugin-conventions`).
 *
 * Fabric Loom and ModDevGradle are compiled for 21, so a 17 target cannot link
 * against them. Anyone building mods with these plugins is on Java 21+ anyway;
 * that floor is the loader tooling's, not ours. Library modules that do not
 * touch loader tooling keep the repo-wide 17 floor from
 * [BaseConventionsPlugin].
 */
class LoaderPluginConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        // Must be set before the base convention runs, since it reads the
        // property during `apply`.
        extra["modkit.bytecodeTarget"] = "21"

        apply<PluginConventionsPlugin>()
    }

}

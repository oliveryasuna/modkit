package com.oliveryasuna.modkit.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/** Name of the entrypoint configuration `mod(...)` adds to. */
internal const val MOD_CONFIGURATION: String = "mod"

/** Name of the entrypoint configuration `nest(...)` adds to. */
internal const val NEST_CONFIGURATION: String = "nest"

/**
 * Declares a mod dependency. `ModkitDependenciesPlugin` mirrors it onto the
 * active base's remapped-mod configuration (Loom `modImplementation` / MDG
 * `implementation`). No-op routing when neither base is applied.
 */
public fun DependencyHandler.mod(notation: Any): Dependency? =
    add(MOD_CONFIGURATION, notation)

/**
 * Declares a jar-in-jar dependency. `ModkitDependenciesPlugin` mirrors it onto
 * the active base's nesting configuration (Loom `include` / MDG `jarJar`). The
 * [action] configures the created dependency — e.g. a
 * `version { strictly(...) }` range, which MDG's `jarJar` honors (Loom's
 * `include` ignores ranges).
 */
public fun DependencyHandler.nest(
    notation: Any,
    action: Action<in ExternalModuleDependency> = Action {}
): Dependency? {
    val dependency = create(notation)
    if(dependency is ExternalModuleDependency) {
        action.execute(dependency)
    }
    return add(NEST_CONFIGURATION, dependency)
}

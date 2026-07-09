package com.oliveryasuna.modkit.dependencies

import com.oliveryasuna.modkit.plugin.applyModkitCore
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/** Gradle property toggling the Modrinth Maven. Default on. */
internal const val MODRINTH_PROPERTY: String = "modkit.dependencies.modrinth"

/** Gradle property toggling CurseMaven. Default off. */
internal const val CURSE_MAVEN_PROPERTY: String = "modkit.dependencies.curseMaven"

/**
 * Exposes `mod(...)`/`nest(...)` dependency sugar, adds the enabled mod
 * repositories, and routes both onto whichever loader base is applied. This
 * plugin never applies a base and never references Loom/MDG types — it mirrors
 * its own `mod`/`nest` bucket configurations onto the base's configurations by
 * name, reacting via `withPlugin`.
 */
public class ModkitDependenciesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Build on the shared model so `modkit` exists for siblings/consumers.
        project.applyModkitCore()

        // Mod repositories are structural: they must exist before Loom/MDG use
        // the repository container (Loom finalizes it in afterEvaluate, and
        // adding a repo afterwards throws). So they are toggled by eagerly-read
        // Gradle properties and added now, not via a lazily-set DSL block —
        // the same reason `modkit.loader`/`modkit.splitClient` are properties.
        if(project.boolProperty(MODRINTH_PROPERTY, default = true)) project.addModrinthRepository()
        if(project.boolProperty(CURSE_MAVEN_PROPERTY, default = false)) project.addCurseMavenRepository()

        // Stable entrypoint buckets — always present so `dependencies { mod(...) }`
        // resolves even before (or without) a base. They collect declarations;
        // the base-specific configurations below actually consume them.
        val mod = project.createBucket(MOD_CONFIGURATION)
        val nest = project.createBucket(NEST_CONFIGURATION)

        // Route onto the applied base. Exactly one ever is; neither → no-op.
        project.pluginManager.withPlugin("fabric-loom") {
            routeInto(project, mod, "modImplementation")
            routeInto(project, nest, "include")
        }
        project.pluginManager.withPlugin("net.neoforged.moddev") {
            routeInto(project, mod, "implementation")
            routeInto(project, nest, "jarJar")
        }
    }

    private fun Project.createBucket(name: String): Configuration =
        configurations.create(name) { config ->
            config.isCanBeResolved = false
            config.isCanBeConsumed = false
        }

    private fun Project.boolProperty(name: String, default: Boolean): Boolean =
        providers.gradleProperty(name).map { it.toBoolean() }.getOrElse(default)
}

/**
 * Mirrors every dependency of [from] onto the base configuration named
 * [targetName]. `all` replays existing declarations and captures later ones, so
 * it is order-independent with respect to when the base plugin applies. Skips
 * silently if the base did not create [targetName].
 */
internal fun routeInto(project: Project, from: Configuration, targetName: String) {
    val target = project.configurations.findByName(targetName) ?: return
    from.dependencies.all { dependency ->
        target.dependencies.add(dependency)
    }
}

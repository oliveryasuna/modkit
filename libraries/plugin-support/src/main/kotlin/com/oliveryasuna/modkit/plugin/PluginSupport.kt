package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.core.manifest.ModkitManifestContributions
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskProvider

/** Applies the `core` plugin so the `modkit` model exists, and returns it. */
public fun Project.applyModkitCore(): ModkitExtension {
    pluginManager.apply("com.oliveryasuna.modkit.core")
    return extensions.getByType(ModkitExtension::class.java)
}

/** Registers a sibling DSL block as an ExtensionAware child of `modkit`. */
public fun <T : Any> ModkitExtension.registerBlock(name: String, type: Class<T>): T =
    (this as ExtensionAware).extensions.create(name, type)

/**
 * Resolves the active loader from the `modkit.loader` property, surfacing a bad
 * value as a Gradle build failure. Read eagerly in `apply()`.
 *
 * Uses [Project.findProperty] rather than `providers.gradleProperty` so it sees
 * an **extra property** as well as a Gradle property
 * (`gradle.properties`/`-P`). `multiversion` sets `modkit.loader` as a per-node
 * extra property (derived from the Stonecutter node name) via a Settings
 * `beforeProject` hook, which cannot be expressed as a global Gradle property;
 * a single-loader project still sets it in `gradle.properties`. Both flow
 * through `findProperty`. Read at configuration time only (never held by a
 * task), so this stays cache-safe.
 */
public fun Project.activeLoader(): McLoader? =
    try {
        McLoader.fromProperty(findProperty(McLoader.PROPERTY)?.toString())
    } catch(e: IllegalArgumentException) {
        throw GradleException(e.message ?: "Invalid '${McLoader.PROPERTY}' value", e)
    }

/**
 * The name of the mod's common (shared) source set — the source set that holds
 * loader-agnostic code and the mod's manifest/resources. Resolved eagerly from
 * the `modkit.commonSourceSet` property, defaulting to `"main"`.
 *
 * Like [activeLoader] and `modkit.splitClient`, this is a **property** rather
 * than a lazy model value: choosing which source set a base binds the mod to,
 * which one holds the generated manifest, and which one the mixin lint scans is
 * a *structural* decision that must be made before the `modkit { }` DSL runs and
 * (on Fabric) before Loom finalizes its configuration — the late, lazy model
 * cannot drive it. Read via [Project.findProperty] so a per-node **extra
 * property** works as well as `gradle.properties`/`-P`.
 */
public fun Project.commonSourceSet(): String =
    findProperty(COMMON_SOURCE_SET_PROPERTY)?.toString()?.takeIf { it.isNotBlank() } ?: DEFAULT_COMMON_SOURCE_SET

/** The property that names the common source set; see [commonSourceSet]. */
public const val COMMON_SOURCE_SET_PROPERTY: String = "modkit.commonSourceSet"

/** The default common source set when `modkit.commonSourceSet` is unset. */
public const val DEFAULT_COMMON_SOURCE_SET: String = "main"

/** Wires a verification task into `check`, when a lifecycle is present. */
public fun Project.wireIntoCheck(task: TaskProvider<out Task>) {
    pluginManager.withPlugin("lifecycle-base") {
        tasks.named("check") { it.dependsOn(task) }
    }
}

/**
 * The shared per-project [ModkitManifestContributions] registry, where sibling
 * plugins publish generated data that `metadata` folds into the mod manifests.
 * Created lazily on first access; safe to call from any modkit plugin.
 */
public fun Project.modkitManifestContributions(): ModkitManifestContributions =
    extensions.findByType(ModkitManifestContributions::class.java)
    ?: extensions.create("modkitManifestContributions", ModkitManifestContributions::class.java)

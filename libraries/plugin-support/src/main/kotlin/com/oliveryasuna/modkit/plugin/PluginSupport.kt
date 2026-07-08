package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
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
 */
public fun Project.activeLoader(): McLoader? =
    try {
        McLoader.fromProperty(providers.gradleProperty(McLoader.PROPERTY).orNull)
    } catch(e: IllegalArgumentException) {
        throw GradleException(e.message ?: "Invalid '${McLoader.PROPERTY}' value", e)
    }

/** Wires a verification task into `check`, when a lifecycle is present. */
public fun Project.wireIntoCheck(task: TaskProvider<out Task>) {
    pluginManager.withPlugin("lifecycle-base") {
        tasks.named("check") { it.dependsOn(task) }
    }
}

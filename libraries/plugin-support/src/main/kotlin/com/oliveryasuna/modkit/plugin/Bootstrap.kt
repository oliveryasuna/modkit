package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

/** The core plugin id. Everything in the suite is built on top of it. */
private const val CORE_PLUGIN_ID: String = "com.oliveryasuna.modkit.core"

/**
 * Applies `core` and hands back the `modkit { }` model.
 *
 * This is line one of every sibling plugin. `core` is what creates the
 * extension, so once this returns the model is there to read.
 */
public fun Project.applyModkitCore(): ModkitExtension {
    pluginManager.apply(CORE_PLUGIN_ID)

    return extensions.getByType(ModkitExtension::class.java)
}

/**
 * Hangs a sibling's own DSL block off the `modkit { }` model.
 *
 * The model is [ExtensionAware], so whatever gets registered here reads as
 * `modkit { <name> { } }` in the build script. Returns the block so the caller
 * can set its conventions right away.
 */
public fun <T : Any> ModkitExtension.registerBlock(
    name: String,
    type: Class<T>
): T = (this as ExtensionAware).extensions.create(name, type)

package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Project

/**
 * One self-contained slice of what applying a plugin does to a project.
 *
 * Breaking `apply()` up this way keeps each concern in its own file and out of
 * the others' way. A plugin just installs them in order.
 */
public fun interface PluginFeature {

    public fun install(
        project: Project,
        model: ModkitExtension
    )

}

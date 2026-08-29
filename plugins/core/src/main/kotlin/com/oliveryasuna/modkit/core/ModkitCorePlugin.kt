package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.ModkitCorePlugin.Companion.features
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.MODKIT_EXTENSION_NAME
import com.oliveryasuna.modkit.plugin.PluginFeature
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The foundation every other Modkit plugin buiilds on.
 *
 * It creates the `modkit { }` model and installs the handful of things every
 * build gets for free: sensible conventions, a derived JVM toolchain, model
 * validation, and the doctor report.
 *
 * The actual work lives in the [features] below, one [PluginFeature] per
 * concern, so this class stays a table of contents.
 */
public class ModkitCorePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val model = project.extensions.create(MODKIT_EXTENSION_NAME, ModkitExtension::class.java)

        features.forEach { it.install(project, model) }
    }

    private companion object {

        // Conventions go first, so the rest see the defaults; after that the
        // order doesn't matter.
        val features: List<PluginFeature> = listOf(
            ModelConventions,
            ModelReport,
            ModelValidation,
            ModkitDoctor
        )

    }

}

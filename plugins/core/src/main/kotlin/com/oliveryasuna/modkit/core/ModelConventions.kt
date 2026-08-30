package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.common.toolchain.JavaToolchainResolver
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.PluginContext
import com.oliveryasuna.modkit.plugin.PluginFeature
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * The defaults.
 *
 * Fills in the parts of the model a user usually shouldn't have to, and works
 * out the JVM toolchain from the Minecraft versions being targeted. Everything
 * here is a convention, so an explicit value in the build script always wins.
 */
internal object ModelConventions : PluginFeature<PluginContext> {

    override fun install(ctx: PluginContext) {
        applyModelDefaults(ctx.project, ctx.model)
        deriveToolchain(ctx.project, ctx.model)
    }

    private fun applyModelDefaults(
        project: Project,
        model: ModkitExtension
    ) {
        with(model) {
            group.convention(project.provider { project.group.toString() })
            version.convention(project.provider { project.version.toString() })
            displayName.convention(modId)
            authors.convention(emptyList())
        }

        // Targets are on by default the moment they are declared.
        model.targets.all { target -> target.enabled.convention(true) }
    }

    private fun deriveToolchain(
        project: Project,
        model: ModkitExtension
    ) {
        // Newer Minecraft wants a newer JDK, so let the declared versions
        // choose.
        model.jvm.toolchain.convention(project.provider {
            JavaToolchainResolver.resolveForTargets(model.targets.map { it.minecraftVersion })
        })

        // Hand the resolved toolchain to the Java plugin, once one is applied.
        project.plugins.withType(JavaBasePlugin::class.java) {
            val java = project.extensions.getByType(JavaPluginExtension::class.java)
            java.toolchain.languageVersion.set(model.jvm.toolchain.map { JavaLanguageVersion.of(it) })
        }
    }

}

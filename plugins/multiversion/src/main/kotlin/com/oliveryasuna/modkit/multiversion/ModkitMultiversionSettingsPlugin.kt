package com.oliveryasuna.modkit.multiversion

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.multiversion.settings.ModkitVersionsSettings
import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Settings-time plugin. Applies Stonecutter, exposes the `modkitVersions { }`
 * block (which expands each mod's version x loader matrix into per-node
 * subprojects), and bridges each node's loader to the `modkit.loader` property
 * that `loaders`/`metadata` read.
 *
 * The loader is derived from the node name and published as a per-node **extra
 * property** through a `beforeProject` hook — which runs before any node's
 * build script, so it is independent of plugin-application order and needs no global
 * Gradle property (a global one cannot vary per node in a single build).
 */
public class ModkitMultiversionSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        settings.pluginManager.apply("dev.kikugie.stonecutter")
        val stonecutter = settings.extensions.getByType(StonecutterSettingsExtension::class.java)

        // Node project path -> loader name, populated as the matrix is declared.
        val nodeLoaders = LinkedHashMap<String, String>()
        settings.extensions.create("modkitVersions", ModkitVersionsSettings::class.java, stonecutter, nodeLoaders)

        settings.gradle.beforeProject { project ->
            nodeLoaders[project.path]?.let { loader ->
                project.extensions.extraProperties.set(McLoader.PROPERTY, loader)
            }
        }
    }
}

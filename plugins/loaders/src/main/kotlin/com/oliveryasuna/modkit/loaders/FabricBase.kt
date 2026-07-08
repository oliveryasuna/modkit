package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project

/**
 * Applies and configures Fabric Loom for a FABRIC target, mapping the Modkit
 * model onto Loom's DSL. Kept internal — no Loom types leak into loaders'
 * public API.
 */
internal fun configureFabric(
    project: Project,
    modkit: ModkitExtension,
    loaders: LoadersSpec
) {
    project.pluginManager.apply("fabric-loom")
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class.java)

    // Active Minecraft version: the single enabled target declaring FABRIC.
    // Resolved lazily — the model DSL is populated after this plugin applies.
    val minecraftVersion = project.provider {
        val fabric = modkit.targets.filter { target ->
            target.enabled.get() && McLoader.FABRIC in target.loaders.get()
        }
        require(fabric.isNotEmpty()) {
            "modkit.loader=fabric but no enabled target declares the fabric loader."
        }
        require(fabric.size == 1) {
            "loaders builds one variant per project, but ${fabric.size} enabled targets declare " +
            "fabric (${fabric.joinToString { it.minecraftVersion }}). Use multiversion or declare one."
        }
        fabric.single().minecraftVersion
    }

    with(project.dependencies) {
        addProvider("minecraft", minecraftVersion.map { "com.mojang:minecraft:$it" })
        add("mappings", loom.officialMojangMappings())
        addProvider("modImplementation", loaders.fabric.loaderVersion.map { "net.fabricmc:fabric-loader:$it" })
    }
}

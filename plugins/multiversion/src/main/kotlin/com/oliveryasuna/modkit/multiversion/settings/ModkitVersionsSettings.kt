package com.oliveryasuna.modkit.multiversion.settings

import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import dev.kikugie.stonecutter.settings.tree.TreeBuilder
import org.gradle.api.Action

/**
 * The `modkitVersions { }` settings block. Declares, per mod, the
 * version x loader matrix; each `mod(":path") { }` is translated **eagerly**
 * into a Stonecutter `create(":path") { }` tree so the node subprojects
 * register during settings evaluation. Node -> loader is recorded into
 * [nodeLoaders] so the settings plugin can set `modkit.loader` per node (see
 * the plugin's `beforeProject`).
 */
public open class ModkitVersionsSettings internal constructor(
    private val stonecutter: StonecutterSettingsExtension,
    private val nodeLoaders: MutableMap<String, String>
) {

    /**
     * The single build script shared by every node. Default `build.gradle.kts`.
     */
    public var centralScript: String = "build.gradle.kts"

    /**
     * Declares the matrix for the mod at Gradle [path] (e.g. `":mods:modA"`).
     * Stonecutter includes the project for you — pass the path string, not
     * `project(...)`.
     */
    public fun mod(path: String, action: Action<in ModVersionSpec>) {
        val spec = ModVersionSpec(path)
        action.execute(spec)
        require(spec.nodes.isNotEmpty()) { "mod('$path') declares no versions" }

        stonecutter.centralScript.set(centralScript)
        stonecutter.create(path, Action<TreeBuilder> { tree ->
            spec.nodes.forEach { node -> tree.version(node.nodeId, node.minecraft) }
            tree.vcsVersion.set(spec.active ?: spec.nodes.first().nodeId)
        })

        // Node project path is "<path>:<nodeId>"; record its loader for the
        // bridge.
        spec.nodes.forEach { node ->
            nodeLoaders["$path:${node.nodeId}"] = node.loader.name.lowercase()
        }
    }
}

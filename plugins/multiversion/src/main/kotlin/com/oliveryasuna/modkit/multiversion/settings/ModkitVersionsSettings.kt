package com.oliveryasuna.modkit.multiversion.settings

import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import dev.kikugie.stonecutter.settings.tree.TreeBuilder
import org.gradle.api.Action
import org.gradle.api.initialization.Settings

/**
 * The `modkitVersions { }` settings block. Declares, per mod, the
 * version x loader matrix; each declaration is translated **eagerly** into a
 * Stonecutter tree so the node subprojects register during settings evaluation.
 * Node -> loader is recorded into [nodeLoaders] so the settings plugin can set
 * `modkit.loader` per node (see the plugin's `beforeProject`).
 *
 * Supports both layouts:
 * - **Monorepo** — `mod(":mods:modA") { }` per mod (nodes at
 *   `:mods:modA:<node>`).
 * - **Single mod at the root** — `root { }` (nodes at `:<node>`).
 */
public open class ModkitVersionsSettings internal constructor(
    private val settings: Settings,
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
        register(label = path, reference = path, parentPath = path, action = action)
    }

    /**
     * Declares the matrix for a **single mod at the repository root** (no
     * subproject). Nodes register directly under the root as `:<mc>-<loader>`.
     */
    public fun root(action: Action<in ModVersionSpec>) {
        register(label = ":", reference = settings.rootProject, parentPath = ":", action = action)
    }

    private fun register(label: String, reference: Any, parentPath: String, action: Action<in ModVersionSpec>) {
        val spec = ModVersionSpec(label)
        action.execute(spec)
        require(spec.nodes.isNotEmpty()) { "mod('$label') declares no versions" }

        stonecutter.centralScript.set(centralScript)
        stonecutter.create(reference, Action<TreeBuilder> { tree ->
            spec.nodes.forEach { node -> tree.version(node.nodeId, node.minecraft) }
            tree.vcsVersion.set(spec.active ?: spec.nodes.first().nodeId)
        })

        // Record each node's project path -> loader for the beforeProject
        // bridge.
        // A root node is ":<nodeId>"; a subproject node is
        // "<parentPath>:<nodeId>".
        spec.nodes.forEach { node ->
            val nodePath = if(parentPath == ":") ":${node.nodeId}" else "$parentPath:${node.nodeId}"
            nodeLoaders[nodePath] = node.loader.name.lowercase()
        }
    }
}

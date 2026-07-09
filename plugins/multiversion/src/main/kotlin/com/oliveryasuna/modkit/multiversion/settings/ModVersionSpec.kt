package com.oliveryasuna.modkit.multiversion.settings

import com.oliveryasuna.modkit.core.extension.McLoader

/** One expanded (version, loader) cell of a mod's matrix. */
internal data class NodeDecl(val nodeId: String, val minecraft: String, val loader: McLoader)

/**
 * Declares the version x loader matrix for a single mod, inside
 * `modkitVersions { mod(":path") { } }`. Each `version(mc, loaders…)` call
 * expands into one Stonecutter node per loader, named `"<mc>-<loader>"`.
 */
public class ModVersionSpec internal constructor(public val path: String) {

    internal val nodes: MutableList<NodeDecl> = mutableListOf()

    /**
     * The node id (`"<mc>-<loader>"`) that is checked out into `src/` by
     * default (Stonecutter's `vcsVersion`). Defaults to the first declared node.
     */
    public var active: String? = null

    /** Declares [minecraft] built for each of [loaders]; one node per loader. */
    public fun version(minecraft: String, vararg loaders: McLoader) {
        require(loaders.isNotEmpty()) { "mod('$path'): version('$minecraft') needs at least one loader" }
        loaders.forEach { loader ->
            val nodeId = "$minecraft-${loader.name.lowercase()}"
            require(nodes.none { it.nodeId == nodeId }) { "mod('$path'): duplicate node '$nodeId'" }
            nodes += NodeDecl(nodeId, minecraft, loader)
        }
    }
}

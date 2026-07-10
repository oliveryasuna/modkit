package com.oliveryasuna.modkit.scaffold

import com.oliveryasuna.modkit.common.version.SemVer
import com.oliveryasuna.modkit.core.extension.McLoader

/** One expanded (Minecraft version, loader) cell of the generated matrix. */
internal data class ScaffoldNode(val minecraft: String, val loader: McLoader) {

    /**
     * Stonecutter node id, `"<mc>-<loader>"` (matches the multiversion module).
     */
    val nodeId: String get() = "$minecraft-${loader.name.lowercase()}"
}

/** The output project shape, chosen by node count. */
internal enum class ScaffoldShape { SIMPLE, MULTIVERSION }

/**
 * A validated, deterministic description of the project to generate. Built once
 * from the task inputs by [ScaffoldPlan.of], then consumed by the pure string
 * renderers. Holds no Gradle types so it can be unit-tested directly.
 */
internal class ScaffoldPlan private constructor(
    val modId: String,
    val group: String,
    val version: String,
    val nodes: List<ScaffoldNode>,
    val modules: List<ScaffoldModule>,
) {

    /** SIMPLE when the matrix is a single node; MULTIVERSION otherwise. */
    val shape: ScaffoldShape
        get() = if(nodes.size == 1) ScaffoldShape.SIMPLE else ScaffoldShape.MULTIVERSION

    /** The distinct Minecraft versions, in declared order. */
    val minecraftVersions: List<String>
        get() = nodes.map { it.minecraft }.distinct()

    companion object {

        private val MOD_ID_REGEX = Regex("[a-z][a-z0-9_-]*")

        /**
         * Validates the raw flag values and expands the version x loader
         * matrix.
         *
         * @throws IllegalArgumentException with a clear message on any invalid
         *                                  modId, version, loader, or module.
         */
        fun of(
            modId: String?,
            group: String,
            version: String,
            versions: List<String>,
            loaders: List<String>,
            modules: List<String>,
        ): ScaffoldPlan {
            val id = modId?.trim().orEmpty()
            require(id.isNotBlank()) {
                "modId is required; pass -PmodId=<your-mod-id>"
            }
            require(MOD_ID_REGEX.matches(id)) {
                "Invalid modId '$id'; must start with a lowercase letter and contain only [a-z0-9_-]"
            }

            require(versions.isNotEmpty()) { "At least one version is required" }
            require(loaders.isNotEmpty()) { "At least one loader is required" }

            // Reject unparseable / snapshot versions up front, with the offending
            // value in the message.
            versions.forEach { raw ->
                try {
                    SemVer.parse(raw.trim())
                } catch(e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid Minecraft version '$raw': ${e.message}", e)
                }
            }

            val resolvedLoaders = loaders.map { raw ->
                McLoader.fromProperty(raw)
                ?: throw IllegalArgumentException("Blank loader value in -Ploaders")
            }

            val resolvedModules = modules
                .filter { it.isNotBlank() }
                .map { ScaffoldModule.fromFlag(it) }
                .distinct()

            // Matrix expansion: versions x loaders, in declared order, deduped.
            val nodes = LinkedHashSet<ScaffoldNode>()
            versions.forEach { v ->
                resolvedLoaders.forEach { loader ->
                    nodes.add(ScaffoldNode(v.trim(), loader))
                }
            }

            return ScaffoldPlan(
                modId = id,
                group = group.trim().ifBlank { "com.example" },
                version = version.trim().ifBlank { "1.0.0" },
                nodes = nodes.toList(),
                modules = resolvedModules,
            )
        }
    }
}

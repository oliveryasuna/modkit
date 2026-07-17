package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.scaffold.ScaffoldModule
import com.oliveryasuna.modkit.scaffold.ScaffoldPlan

/**
 * Renders the per-module DSL blocks that go inside `modkit { }`. Each block's
 * shape is taken verbatim from the module's shipped extension types:
 *
 * - `metadata { entrypoints { main("...") } }` (MetadataSpec/EntrypointsSpec)
 * - `mixins { register("<modId>") { pkg.set("...") } }`
 *   (MixinsSpec/MixinConfig)
 * - `run` needs no configuration (RunSpec defaults are sufficient)
 *
 * `publish`/`ci`/`dependencies`/`datagen` require consumer-specific tokens or
 * targets that a generic scaffold cannot invent, so applying the plugin is the
 * complete, sensible default — no block is emitted for them.
 */
internal object ModuleBlocks {

    /**
     * Returns block lines (already indented four spaces) for each selected
     * module.
     */
    fun render(plan: ScaffoldPlan): List<String> {
        val lines = mutableListOf<String>()

        if(plan.modules.contains(ScaffoldModule.METADATA)) {
            lines += "    metadata {"
            lines += "        entrypoints { main(\"${Naming.modClassFqcn(plan)}\") }"
            lines += "    }"
        }

        if(plan.modules.contains(ScaffoldModule.MIXINS)) {
            lines += "    mixins {"
            lines += "        register(\"${plan.modId}\") { pkg.set(\"${Naming.basePackage(plan)}.mixin\") }"
            lines += "    }"
        }

        return lines
    }

    /**
     * The plugin-id application lines (`id("...")`) for each selected module.
     */
    fun pluginIds(plan: ScaffoldPlan): List<String> =
        plan.modules.map { "    id(\"${it.pluginId}\")" }

    /**
     * The `loaders { }` block (indented four spaces to sit inside
     * `modkit { }`), with a `fabric`/`neoforge` sub-block per selected loader.
     * The loader/API versions have no built-in default, so they are emitted as
     * `TODO` placeholders the user must fill — otherwise the build compiles but
     * the run fails (no `fabric-loader` on the classpath, etc.).
     */
    fun loadersBlock(plan: ScaffoldPlan): List<String> {
        val loaders = plan.nodes.map { it.loader }.distinct()
        val lines = mutableListOf<String>()

        lines += "    loaders {"
        if(loaders.contains(McLoader.FABRIC)) {
            lines += "        fabric {"
            lines += "            // TODO: loaderVersion.set(\"VERSION\")"
            lines += "            // TODO: apiVersion.set(\"VERSION\")  // optional"
            lines += "        }"
        }
        if(loaders.contains(McLoader.NEOFORGE)) {
            lines += "        neoforge {"
            lines += "            // TODO: version.set(\"VERSION\")"
            lines += "        }"
        }
        lines += "    }"

        return lines
    }
}

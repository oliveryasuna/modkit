package com.oliveryasuna.modkit.scaffold.render

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
}

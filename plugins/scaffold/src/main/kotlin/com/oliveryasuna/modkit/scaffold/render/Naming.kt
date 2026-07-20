package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.scaffold.ScaffoldPlan
import com.oliveryasuna.modkit.scaffold.ScaffoldShape
import com.oliveryasuna.modkit.scaffold.render.Naming.basePackage

/**
 * Deterministic naming derived from a [ScaffoldPlan]. Pure functions only — no
 * Gradle types, no I/O — so the renderers stay directly testable.
 */
internal object Naming {

    /**
     * he base package of the generated example source,
     * `<group>.<sanitized-modId>`.
     */
    fun basePackage(plan: ScaffoldPlan): String =
        "${plan.group}.${plan.modId.replace('-', '_')}"

    /** Source-tree path for [basePackage], e.g. `com/example/mymod`. */
    fun basePackagePath(plan: ScaffoldPlan): String =
        basePackage(plan).replace('.', '/')

    /** The example mod entry class simple name, PascalCase from the modId. */
    fun modClassName(plan: ScaffoldPlan): String =
        plan.modId
            .split('-', '_')
            .filter { it.isNotEmpty() }
            .joinToString("") { part -> part.replaceFirstChar { it.uppercaseChar() } }

    /** A loader's package/name segment, e.g. `"fabric"` / `"neoforge"`. */
    fun loaderTag(loader: McLoader): String = loader.name.lowercase()

    private fun loaderPascal(loader: McLoader): String =
        loaderTag(loader).replaceFirstChar { it.uppercaseChar() }

    /**
     * The entry class simple name for [loader]: bare in the single-loader
     * simple shape (e.g. `Mymod`), loader-suffixed in the multiversion shape
     * where both loaders' bootstraps share one source set (e.g. `MymodFabric`).
     */
    fun entryClassName(plan: ScaffoldPlan, loader: McLoader): String =
        if(plan.shape == ScaffoldShape.SIMPLE) modClassName(plan)
        else modClassName(plan) + loaderPascal(loader)

    /**
     * The entry class package for [loader]: the base package in the simple
     * shape, a per-loader sub-package (`<base>.fabric` / `<base>.neoforge`) in
     * the multiversion shape.
     */
    fun entryPackage(plan: ScaffoldPlan, loader: McLoader): String =
        if(plan.shape == ScaffoldShape.SIMPLE) basePackage(plan)
        else "${basePackage(plan)}.${loaderTag(loader)}"

    /** Fully-qualified entry class for [loader]. */
    fun entryFqcn(plan: ScaffoldPlan, loader: McLoader): String =
        "${entryPackage(plan, loader)}.${entryClassName(plan, loader)}"

    /**
     * The Fabric entry FQCN wired into `fabric.mod.json` `entrypoints.main`,
     * or `null` when Fabric is not in the matrix (NeoForge uses `@Mod`, no
     * manifest entry).
     */
    fun fabricEntryFqcn(plan: ScaffoldPlan): String? =
        if(plan.nodes.any { it.loader == McLoader.FABRIC }) entryFqcn(plan, McLoader.FABRIC) else null
}

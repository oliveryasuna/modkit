package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.scaffold.ScaffoldPlan
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

    /** Fully-qualified example mod entry class, `<basePackage>.<ClassName>`. */
    fun modClassFqcn(plan: ScaffoldPlan): String =
        "${basePackage(plan)}.${modClassName(plan)}"
}

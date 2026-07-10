package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.scaffold.ScaffoldPlan
import com.oliveryasuna.modkit.scaffold.ScaffoldShape

/**
 * Entry point for rendering: dispatches on the plan's [ScaffoldShape] to the
 * simple or multiversion renderer. Returns the full, ordered set of files to
 * write. Deterministic — the same plan yields identical bytes.
 */
internal object ScaffoldRenderer {

    fun render(plan: ScaffoldPlan): List<GeneratedFile> =
        when(plan.shape) {
            ScaffoldShape.SIMPLE -> SimpleRenderer.render(plan)
            ScaffoldShape.MULTIVERSION -> MultiversionRenderer.render(plan)
        }
}

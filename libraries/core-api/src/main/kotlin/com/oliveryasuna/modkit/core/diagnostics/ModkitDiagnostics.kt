package com.oliveryasuna.modkit.core.diagnostics

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty

/**
 * Inter-plugin diagnostics registry: each applied plugin publishes a titled
 * section of report lines (and optional non-failing problem warnings) that the
 * `modkitDoctor` task folds into one health summary — with no cross-plugin
 * implementation dependency.
 *
 * A shared, plugin-internal registry (distinct from the user-facing
 * `modkit { }` model, and a sibling of `ModkitManifestContributions`) obtained
 * via `plugin-support`'s `Project.modkitDiagnostics()`. Contributions are wired
 * lazily (each section is a `Provider`-backed value), so `modkitDoctor` reads
 * fully-resolved values at execution time.
 */
public abstract class ModkitDiagnostics {

    /**
     * Report sections keyed by title (e.g. `"Model"`, `"Loader"`, `"Runs"`),
     * each a list of already-formatted lines. Insertion order is preserved, so
     * the first contributor's section prints first.
     */
    public abstract val sections: MapProperty<String, List<String>>

    /**
     * Non-failing problem warnings surfaced under the report's "Problems"
     * section (e.g. no targets declared, no active loader). `modkitDoctor`
     * reports but never fails on these.
     */
    public abstract val problems: ListProperty<String>

}

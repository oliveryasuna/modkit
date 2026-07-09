package com.oliveryasuna.modkit.multiversion.extension

import org.gradle.api.Action

/**
 * The `modkit { multiversion { } }` block. `onVersion(range)` applies its
 * overrides only when the node's Stonecutter version matches [range] (semver
 * predicates like `">=1.21"`, `"<1.20.5 || >=1.21"`). Outside a Stonecutter
 * (multi-version) build the block is inert — no node, nothing matches.
 */
public abstract class MultiversionSpec {

    // Bound by the plugin on node projects; null elsewhere -> onVersion is a no-op.
    internal var runtime: MultiversionRuntime? = null

    /**
     * Applies [action]'s overrides iff the current node version satisfies
     * [range].
     */
    public fun onVersion(range: String, action: Action<in VersionOverride>) {
        val runtime = this.runtime ?: return
        if(!runtime.matches(range)) return

        val override = runtime.newOverride()
        action.execute(override)
        runtime.apply(override)
    }
}

/**
 * Wiring the plugin injects so [MultiversionSpec] can evaluate + apply
 * overrides.
 */
internal interface MultiversionRuntime {
    fun matches(range: String): Boolean
    fun newOverride(): VersionOverride
    fun apply(override: VersionOverride)
}

package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec

/**
 * The loader-agnostic "kind" of a run.
 *
 * Both the fixed runs (`client`/`server`/`data`/`gametest`) and the compat-test
 * variants map onto these; each backend decides how to realize a kind, and
 * which kinds it simply cannot do.
 */
internal enum class RunKind {

    CLIENT,

    SERVER,

    DATA,

    GAMETEST;

    /**
     * The run name a variant produces for this kind, e.g.,
     * `CLIENT` + `"modMenu"` gives `"clientModMenu"` (Loom/MDG then derive the
     * task `runClientModMenu`).
     */
    fun runName(variant: String): String =
        name.lowercase() + variant.replaceFirstChar { it.uppercaseChar() }

    companion object {

        /**
         * Resolves an `appliesTo(...)` string to a kind, or `null` if it names
         * none.
         */
        fun fromName(name: String): RunKind? =
            RunKind.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    }

}

/** The fixed [RunConfig] a variant of [kind] clones its values from. */
internal fun RunSpec.runByKind(kind: RunKind): RunConfig =
    when(kind) {
        RunKind.CLIENT -> client
        RunKind.SERVER -> server
        RunKind.DATA -> data
        RunKind.GAMETEST -> gametest
    }

package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec

/**
 * The loader-agnostic "kind" of a run. Both the fixed runs (`client`/`server`/
 * `data`/`gametest`) and compat-test variants map onto these; the loader-
 * specific code decides how each kind is realized (and which are unsupported on
 * a given loader).
 */
internal enum class RunKind {

    CLIENT,
    SERVER,
    DATA,
    GAMETEST;

    /**
     * The run name a variant produces for this kind, e.g. `CLIENT` +
     * `"modMenu"` -> `"clientModMenu"` (Loom/MDG then derive the task
     * `runClientModMenu`).
     */
    fun runName(variant: String): String =
        name.lowercase() + variant.replaceFirstChar { it.uppercaseChar() }

    internal companion object {

        /**
         * Resolves an `appliesTo(...)` string to a kind, or `null` if unknown.
         */
        fun fromName(name: String): RunKind? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    }

}

/**
 * The fixed [RunConfig] a variant of the given [kind] clones its values from.
 */
internal fun RunSpec.runByKind(kind: RunKind): RunConfig =
    when(kind) {
        RunKind.CLIENT -> client
        RunKind.SERVER -> server
        RunKind.DATA -> data
        RunKind.GAMETEST -> gametest
    }

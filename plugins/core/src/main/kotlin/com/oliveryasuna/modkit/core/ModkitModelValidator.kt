package com.oliveryasuna.modkit.core

/**
 * Pure validation of the resolved Modkit model. Kept free of Gradle types so it
 * can be unit-tested directly and so the task action only feeds it plain
 * snapshots (config-cache friendly).
 */
internal object ModkitModelValidator {

    val MOD_ID_REGEX: Regex = Regex("^[a-z][a-z0-9_-]{1,63}$")

    /**
     * A target reduced to what validation needs — name and whether it declares
     * any loader.
     */
    internal data class TargetView(
        val name: String,
        val hasLoaders: Boolean,
    )

    /**
     * Returns human-readable error messages; empty list means the model is
     * valid.
     */
    fun validate(modId: String?, targets: List<TargetView>): List<String> = buildList {
        when {
            modId.isNullOrBlank() ->
                add("modId is required")

            !MOD_ID_REGEX.matches(modId) ->
                add("modId '$modId' must match ${MOD_ID_REGEX.pattern}")
        }

        if(targets.isEmpty()) {
            add("at least one target is required")
        }
        targets.forEach { target ->
            if(!target.hasLoaders) {
                add("target '${target.name}' must declare at least one loader")
            }
        }
    }

}

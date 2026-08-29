package com.oliveryasuna.modkit.core

/**
 * The model validation rules, with no Gradle in sight.
 *
 * Keeping it pure means it unit-tests directly, and the validation task only
 * ever hands it plain snapshots, which is what keeps the task
 * configuration-cache clean.
 */
internal object ModkitModelValidator {

    /** A mod id is lower-case, starts with a letter, and runs 2 to 64 chars. */
    val MOD_ID_PATTERN: Regex = Regex("^[a-z][a-z0-9_-]{1,63}$")

    /** Just the parts of a target that validation actually looks at. */
    data class TargetView(
        val name: String,
        val hasLoaders: Boolean,
    )

    /**
     * Runs the checks and returns one message per problem. An empty list means
     * the model is good.
     */
    fun validate(modId: String?, targets: List<TargetView>): List<String> = buildList {
        when {
            modId.isNullOrBlank() -> add("modId is required")
            !MOD_ID_PATTERN.matches(modId) -> add("modId '$modId' must match ${MOD_ID_PATTERN.pattern}")
        }

        if(targets.isEmpty()) {
            add("at least one target is required")
        }

        targets.filterNot { it.hasLoaders }.forEach { target ->
            add("target '${target.name}' must declare at least one loader")
        }
    }


}

package com.oliveryasuna.modkit.metadata

import com.electronwill.nightconfig.core.Config
import org.gradle.api.GradleException

/**
 * Expands `${token}` placeholders in the string values of a built manifest,
 * using values drawn from the mod model and active target. Opt-in via
 * `modkit.metadata.substituteTokens` — off by default, so a literal `${...}`
 * passes through untouched unless the user asks for expansion.
 *
 * Substitution is applied to string *values* only (not keys), recursively over
 * the assembled config — so identity fields, descriptions, and raw overrides
 * are all covered uniformly. Unknown tokens fail fast: a `${typo}` is a build
 * error listing the valid tokens, which catches mistakes rather than silently
 * shipping a broken manifest.
 */
internal object TokenSubstitution {

    /** Matches `${name}` where name is any run of non-`}` characters. */
    private val TOKEN = Regex("""\$\{([^}]*)}""")

    /**
     * The known tokens and their resolved values. A value may be `null` when it
     * is unavailable (e.g. `${minecraft}` with no active target) — using such a
     * token is a fail-fast error, distinct from an unknown token.
     */
    fun tokensFrom(inputs: ManifestInputs): Map<String, String?> =
        linkedMapOf(
            "version" to inputs.version,
            "modId" to inputs.modId,
            "name" to inputs.displayName,
            "group" to inputs.group,
            "minecraft" to inputs.minecraftVersion
        )

    /**
     * Recursively expands tokens in every string value of [config], in place.
     */
    fun apply(config: Config, tokens: Map<String, String?>) {
        for(entry in config.entrySet()) {
            entry.setValue<Any?>(substituteValue(entry.getValue(), tokens))
        }
    }

    private fun substituteValue(value: Any?, tokens: Map<String, String?>): Any? =
        when(value) {
            is String -> substitute(value, tokens)
            is Config -> {
                apply(value, tokens)
                value
            }

            is List<*> -> value.map { substituteValue(it, tokens) }
            else -> value
        }

    /**
     * Expands every `${token}` in [text], failing fast on unknown/unavailable
     * tokens.
     */
    fun substitute(text: String, tokens: Map<String, String?>): String =
        TOKEN.replace(text) { match ->
            val name = match.groupValues[1]
            when {
                !tokens.containsKey(name) -> throw GradleException(
                    "Unknown token '\${$name}' in mod metadata. Valid tokens: " +
                    tokens.keys.joinToString { "\${$it}" } + "."
                )

                tokens[name] == null -> throw GradleException(
                    "Token '\${$name}' is unavailable — no active Minecraft target provides a value for it."
                )

                else -> tokens.getValue(name)!!
            }
        }

}

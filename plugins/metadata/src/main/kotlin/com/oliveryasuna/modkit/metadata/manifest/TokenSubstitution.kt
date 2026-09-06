package com.oliveryasuna.modkit.metadata.manifest

import com.electronwill.nightconfig.core.Config
import org.gradle.api.GradleException

/**
 * Expands `${token}` placeholders in a built manifest's string values.
 *
 * Opt-in (off by default), so a literal `${...}` passes straight through
 * unless the user asks for expansion. Only string *values* are touched, never
 * keys, and the whole config is walked so identity fields, descriptions, and
 * raw overrides are all covered. Unknown tokens fail the build rather than
 * shipping a broken manifest.
 */
internal object TokenSubstitution {

    /** Matches `${name}`, where name is any run of non-`}` characters. */
    private val TOKEN = Regex("""\$\{([^}]*)}""")

    /**
     * The known tokens and their values. A value may be `null` when it is
     * unavailable (for example `${minecraft}` with no active target); using
     * such a token is its own fail-fast error, separate from an unknown one.
     */
    fun tokensFrom(inputs: ManifestInputs): Map<String, String?> =
        linkedMapOf(
            "version" to inputs.version,
            "modId" to inputs.modId,
            "name" to inputs.displayName,
            "group" to inputs.group,
            "minecraft" to inputs.minecraftVersion,
        )

    /** Walks [config], expanding tokens in every string value in place. */
    fun apply(config: Config, tokens: Map<String, String?>) {
        for(entry in config.entrySet()) {
            entry.setValue<Any?>(expandValue(entry.getValue(), tokens))
        }
    }

    private fun expandValue(value: Any?, tokens: Map<String, String?>): Any? =
        when(value) {
            is String -> expand(value, tokens)
            is Config -> value.also { apply(it, tokens) }
            is List<*> -> value.map { expandValue(it, tokens) }
            else -> value
        }

    /**
     * Expands every `${token}` in [text], failing fast on an unknown or
     * unavailable one.
     */
    fun expand(text: String, tokens: Map<String, String?>): String =
        TOKEN.replace(text) { match ->
            val name = match.groupValues[1]
            when {
                name !in tokens -> throw GradleException(
                    "Unknown token '\${$name}' in mod metadata. Valid tokens: " +
                            tokens.keys.joinToString { $$"${$$it}" } + ".",
                )

                tokens[name] == null -> throw GradleException(
                    $$"Token '${$$name}' is unavailable; no active Minecraft target provides a value for it.",
                )

                else -> tokens.getValue(name)!!
            }
        }
}

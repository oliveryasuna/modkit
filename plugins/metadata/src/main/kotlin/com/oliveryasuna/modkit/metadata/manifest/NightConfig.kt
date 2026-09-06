package com.oliveryasuna.modkit.metadata.manifest

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat

/**
 * The little bit of NightConfig plumbing both manifest builders share.
 *
 * Everything here keeps insertion order (backing every config with a
 * [LinkedHashMap]) so a given model always serializes to the same bytes, which
 * is what makes the generate tasks cacheable.
 */

/** A fresh config whose keys stay in the order they are added. */
internal fun orderedConfig(format: ConfigFormat<out Config>): Config =
    format.createConfig { LinkedHashMap<String, Any>() }

/**
 * Merges user-supplied raw overrides into [this], last, so they win over the
 * generated values. Keys are set as single-element paths so a dotted key is one
 * key, not a nested path.
 */
internal fun Config.mergeRaw(format: ConfigFormat<out Config>, raw: Map<String, Any>) {
    for((key, value) in raw) {
        set<Any?>(listOf(key), value.toConfigValue(format))
    }
}

/**
 * Recursively converts a plain value (maps, lists, scalars) into NightConfig's
 * shape.
 */
private fun Any.toConfigValue(format: ConfigFormat<out Config>): Any =
    when(this) {
        is Map<*, *> -> orderedConfig(format).also { nested ->
            for((key, value) in this) {
                if(key != null && value != null) {
                    nested.set<Any?>(listOf(key.toString()), value.toConfigValue(format))
                }
            }
        }

        is List<*> -> mapNotNull { element -> element?.toConfigValue(format) }
        else -> this
    }

package com.oliveryasuna.modkit.core.extension

public enum class McLoader {

    FABRIC,

    NEOFORGE;

    /**
     * Shared contract for the `modkit.loader` Gradle property, which selects
     * the active loader. Kept free of Gradle types and I/O so it can be reused
     * and unit-tested directly.
     */
    public companion object {

        /** The Gradle property name that selects the active loader. */
        public const val PROPERTY: String = "modkit.loader"

        /**
         * Maps a raw `modkit.loader` value to its [McLoader], or `null` when
         * unset/blank. Matches [entries] case-insensitively by name.
         *
         * @throws IllegalArgumentException if [raw] is non-blank but names no
         *                                  known loader.
         */
        public fun fromProperty(raw: String?): McLoader? {
            val value = raw?.trim()
            if(value.isNullOrEmpty()) return null

            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                   ?: throw IllegalArgumentException("Unknown '$PROPERTY' value '$value'; expected one of " + entries.joinToString { it.name.lowercase() })
        }

    }

}

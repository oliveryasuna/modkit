package com.oliveryasuna.modkit.core.extension

/** A mod loader Modkit can build for. */
public enum class ModLoader {

    FABRIC,
    NEOFORGE;
    // TODO: Support Forge and Quilt.

    public companion object {

        /** Gradle property that names the loader the current build targets. */
        public const val PROPERTY: String = "modkit.loader"

        /**
         * Reads a `modkit.loader` value into a [ModLoader].
         *
         * Plugins are applied inside `plugins { }`, before the `modkit { }`
         * block runs, so a plugin can't get its loader from the model. It gets
         * it from this property instead. No Gradle types here, so the parsing
         * is easy to test on its own.
         *
         * Blank or missing gives `null` (a project doesn't always have an
         * active) loader. An unrecognized value throws, so types don't quietly
         * fall back to some default.
         */
        public fun fromProperty(raw: String?): ModLoader? {
            val name = raw?.trim().orEmpty()
            if(name.isEmpty()) {
                return null
            }

            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown '${PROPERTY}' value '${name}'; expected one of ${entries.joinToString { it.name.lowercase() }}.")
        }

    }

}

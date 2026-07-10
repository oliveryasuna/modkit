package com.oliveryasuna.modkit.scaffold

/**
 * The Modkit modules a generated project can opt into. Each maps to the Gradle
 * plugin id the generated `build.gradle.kts` applies. `loaders`/`multiversion`
 * (the base) and `core` (transitive) are never listed here — they are always
 * emitted by the renderer.
 */
internal enum class ScaffoldModule(val flag: String, val pluginId: String) {

    METADATA("metadata", "com.oliveryasuna.modkit.metadata"),
    MIXINS("mixins", "com.oliveryasuna.modkit.mixins"),
    RUN("run", "com.oliveryasuna.modkit.run"),
    PUBLISH("publish", "com.oliveryasuna.modkit.publish"),
    CI("ci", "com.oliveryasuna.modkit.ci"),
    DEPENDENCIES("dependencies", "com.oliveryasuna.modkit.dependencies"),
    DATAGEN("datagen", "com.oliveryasuna.modkit.datagen");

    companion object {

        /**
         * Resolves a raw `-Pmodules` token to a [ScaffoldModule], matching the
         * flag case-insensitively.
         *
         * @throws IllegalArgumentException if [raw] names no known module.
         */
        fun fromFlag(raw: String): ScaffoldModule {
            val value = raw.trim()
            return entries.firstOrNull { it.flag.equals(value, ignoreCase = true) }
                   ?: throw IllegalArgumentException("Unknown module '$value'; expected one of ${entries.joinToString { it.flag }}")
        }
    }
}

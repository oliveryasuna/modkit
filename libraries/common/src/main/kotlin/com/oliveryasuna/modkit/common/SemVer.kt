package com.oliveryasuna.modkit.common

public data class SemVer(
    public val major: Int,
    public val minor: Int,
    public val patch: Int = 0,
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int =
        compareValuesBy(this, other, SemVer::major, SemVer::minor, SemVer::patch)

    override fun toString(): String = "$major.$minor.$patch"

    public companion object {

        /**
         * Parses "MAJOR", "MAJOR.MINOR", or "MAJOR.MINOR.PATCH".
         * Missing minor/patch default to 0. Any suffix after the third
         * component (e.g. "-rc1", "+build") is rejected.
         *
         * @throws IllegalArgumentException if [value] is not 1–3 dot-separated
         *                                  integers.
         */
        public fun parse(value: String): SemVer {
            require(value.isNotBlank()) { "Version string is blank" }

            val parts = value.split('.')
            require(parts.size in 1..3) { "Unsupported version format: '$value'" }

            val nums = parts.map { part ->
                part.toIntOrNull()
                ?: throw IllegalArgumentException("Non-numeric component in version: '$value'")
            }
            require(nums.all { it >= 0 }) { "Negative component in version: '$value'" }

            return SemVer(
                major = nums[0],
                minor = nums.getOrElse(1) { 0 },
                patch = nums.getOrElse(2) { 0 },
            )
        }

        /** Non-throwing variant; returns null on malformed input. */
        public fun parseOrNull(value: String): SemVer? =
            runCatching { parse(value) }.getOrNull()
    }
}

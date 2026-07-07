package com.oliveryasuna.modkit.common.toolchain

import com.oliveryasuna.modkit.common.version.SemVer

public object JavaToolchainResolver {

    // Ordered floors; first matching lower bound wins. Boundaries per Minecraft
    // wiki.
    private val floors: List<Pair<SemVer, Int>> = listOf(
        SemVer(26, 1, 0) to 25,
        SemVer(1, 20, 5) to 21,
        SemVer(1, 18, 0) to 17,
        SemVer(1, 17, 0) to 16,
        SemVer(1, 12, 0) to 8
    )

    public fun minimumJdkFor(minecraftVersion: String): Int {
        val v = SemVer.Companion.parse(minecraftVersion)

        // Conservative fallback to 8 for very old versions.
        return floors.firstOrNull { (floor, _) -> v >= floor }?.second ?: 8
    }

    // For a multi-loader target, one JVM compiles to all its loaders -> take
    // the max floor. Empty input has no versions to compare, so it resolves to
    // the newest known floor (25) rather than throwing — this feeds
    // `JavaLanguageVersion.of` in `core` and must never be invalid.
    public fun resolveForTargets(versions: Iterable<String>): Int =
        versions.maxOfOrNull(::minimumJdkFor) ?: 25

}

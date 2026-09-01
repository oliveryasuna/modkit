package com.oliveryasuna.modkit.common.toolchain

import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.withoutSuffixes

public object JavaToolchainResolver {

    // Minimum Java per Minecraft release, newest first.
    // https://minecraft.wiki/w/Tutorial:Update_Java#Why_update?
    private val floors = listOf(
        "26.1" to 25,
        "1.20.5" to 21,
        "1.18" to 17,
        "1.17" to 16,
        "1.12" to 8
    ).map { (mc, java) -> mc.toVersion(strict = false) to java }

    /**
     * @throws io.github.z4kn4fein.semver.VersionFormatException if
     *         [minecraftVersion] is not a release-style version ("1.21",
     *         "1.20.5", "26.1"). Snapshot ids ("24w14a") are not supported.
     */
    public fun minimumJdkFor(minecraftVersion: String): Int {
        // Pre-releases already carry the requirement of the release they lead
        // up to (e.g., 24w14a needed 21), so drop the suffix before comparing.
        val version = minecraftVersion.toVersion(strict = false).withoutSuffixes()

        return floors.firstOrNull { (floor, _) -> version >= floor }?.second
            ?: floors.last().second
    }

    // One JVM builds every loader of a target, so take the highest floor. No
    // targets means nothing to constrain; fall back to the newest floor rather
    // than fail, since this feeds `JavaLanguageVersion.of()` is `core`.
    public fun resolveForTargets(versions: Iterable<String>): Int =
        versions.maxOfOrNull(::minimumJdkFor) ?: floors.first().second

}

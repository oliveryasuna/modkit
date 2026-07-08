package com.oliveryasuna.modkit.publish

import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException

/**
 * Maps the Modkit release-type string onto the upstream release-type enum. Kept
 * internal so the upstream enum does not leak into publish's public API.
 */
internal object ReleaseTypes {

    /**
     * Maps `stable` / `beta` / `alpha` (case-insensitive) to [ReleaseType], and
     * surfaces anything else as a Gradle-friendly build failure.
     */
    fun of(raw: String): ReleaseType =
        when(raw.trim().lowercase()) {
            "stable" -> ReleaseType.STABLE
            "beta" -> ReleaseType.BETA
            "alpha" -> ReleaseType.ALPHA
            else -> throw GradleException("Unknown publish type '$raw'; expected one of stable, beta, alpha.")
        }

}

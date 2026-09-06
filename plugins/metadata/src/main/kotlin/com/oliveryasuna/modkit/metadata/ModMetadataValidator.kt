package com.oliveryasuna.modkit.metadata

import io.github.z4kn4fein.semver.toVersionOrNull

/** The metadata validation rules, with no Gradle in sight. */
internal object ModMetadataValidator {

    fun validate(
        version: String?,
        icon: String?,
        iconExists: Boolean,
        license: String?,
        isNeoForge: Boolean,
        failOnMissingIcon: Boolean,
        failOnInvalidSemver: Boolean,
        failOnUndeclaredMixinConfig: Boolean,
        failOnMissingLicense: Boolean
    ): List<String> = buildList {
        if(failOnInvalidSemver && (version == null || version.toVersionOrNull() == null)) {
            add("Mod version '${version}' is not a valid semver (expected MAJOR.MINOR.PATCH).")
        }

        // NeoForge rejects a mod with no license.
        if(failOnMissingLicense && isNeoForge && license.isNullOrBlank()) {
            add("NeoForge requires a license; set `modkit.license` (e.g. \"MIT\" or \"All Rights Reserved\").")
        }

        // "Missing" means declared-but-absent: only fail when an icon is named
        // and the file cannot be found under the common source set's resources.
        if(failOnMissingIcon && icon != null && !iconExists) {
            add("Declared icon '${icon}' was not found in the common source set's resources.")
        }

        // TODO: Handle `failOnUndeclaredMixinConfig`.
    }

}

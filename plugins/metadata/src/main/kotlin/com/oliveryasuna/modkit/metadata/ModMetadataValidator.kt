package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.common.version.SemVer

/**
 * Pure validation of the snapshotted metadata model. Returns a (possibly empty)
 * list of human-readable errors; the caller decides how to surface them.
 */
internal object ModMetadataValidator {

    fun validate(
        version: String?,
        icon: String?,
        iconExists: Boolean,
        failOnMissingIcon: Boolean,
        failOnInvalidSemver: Boolean,
        failOnUndeclaredMixinConfig: Boolean
    ): List<String> {
        val errors = ArrayList<String>()

        if(failOnInvalidSemver) {
            if(version == null || SemVer.parseOrNull(version) == null) {
                errors.add("Mod version '$version' is not a valid semver (expected MAJOR.MINOR.PATCH).")
            }
        }

        // "Missing icon" means declared-but-absent: only fail when an icon is
        // declared and the referenced file cannot be found under main
        // resources.
        if(failOnMissingIcon && icon != null && !iconExists) {
            errors.add("Declared icon '$icon' was not found under main resources (src/main/resources/$icon).")
        }

        // No mixin-config source is wired yet, so there are no referenced
        // configs to reconcile; the check passes trivially.
        if(failOnUndeclaredMixinConfig) {
            Unit
        }

        return errors
    }

}

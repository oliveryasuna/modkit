package com.oliveryasuna.modkit.metadata

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModMetadataValidatorTest {

    private fun validate(
        version: String? = "1.0.0",
        icon: String? = null,
        iconExists: Boolean = false,
        license: String? = "MIT",
        isNeoForge: Boolean = false,
        failOnMissingIcon: Boolean = true,
        failOnInvalidSemver: Boolean = true,
        failOnUndeclaredMixinConfig: Boolean = true,
        failOnMissingLicense: Boolean = true
    ): List<String> =
        ModMetadataValidator.validate(
            version = version,
            icon = icon,
            iconExists = iconExists,
            license = license,
            isNeoForge = isNeoForge,
            failOnMissingIcon = failOnMissingIcon,
            failOnInvalidSemver = failOnInvalidSemver,
            failOnUndeclaredMixinConfig = failOnUndeclaredMixinConfig,
            failOnMissingLicense = failOnMissingLicense
        )

    @Test
    fun `a valid semver passes`() {
        assertTrue(validate(version = "1.2.3").isEmpty())
    }

    @Test
    fun `an invalid semver fails`() {
        val errors = validate(version = "not-a-semver")
        assertTrue(errors.any { it.contains("semver") }, errors.toString())
    }

    @Test
    fun `semver check can be disabled`() {
        assertTrue(validate(version = "not-a-semver", failOnInvalidSemver = false).isEmpty())
    }

    @Test
    fun `a declared but missing icon file fails`() {
        val errors = validate(icon = "assets/mymod/icon.png", iconExists = false)
        assertTrue(errors.any { it.contains("icon") }, errors.toString())
    }

    @Test
    fun `a declared and present icon file passes`() {
        assertTrue(validate(icon = "assets/mymod/icon.png", iconExists = true).isEmpty())
    }

    @Test
    fun `an unset icon does not fail`() {
        assertTrue(validate(icon = null).isEmpty())
    }

    @Test
    fun `the mixin config check is a no-op for now`() {
        assertFalse(
            validate(failOnUndeclaredMixinConfig = true).any { it.contains("mixin") }
        )
    }

    @Test
    fun `NeoForge without a license fails`() {
        val errors = validate(license = null, isNeoForge = true)
        assertTrue(errors.any { it.contains("license") }, errors.toString())
    }

    @Test
    fun `NeoForge with a blank license fails`() {
        assertTrue(validate(license = "  ", isNeoForge = true).any { it.contains("license") })
    }

    @Test
    fun `NeoForge with a license passes`() {
        assertTrue(validate(license = "MIT", isNeoForge = true).isEmpty())
    }

    @Test
    fun `Fabric without a license does not fail`() {
        assertTrue(validate(license = null, isNeoForge = false).isEmpty())
    }

    @Test
    fun `the license check can be disabled`() {
        assertTrue(validate(license = null, isNeoForge = true, failOnMissingLicense = false).isEmpty())
    }

}

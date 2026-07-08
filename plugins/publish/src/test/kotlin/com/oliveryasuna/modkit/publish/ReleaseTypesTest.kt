package com.oliveryasuna.modkit.publish

import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ReleaseTypesTest {

    @Test
    fun `maps the canonical names`() {
        assertEquals(ReleaseType.STABLE, ReleaseTypes.of("stable"))
        assertEquals(ReleaseType.BETA, ReleaseTypes.of("beta"))
        assertEquals(ReleaseType.ALPHA, ReleaseTypes.of("alpha"))
    }

    @Test
    fun `is case-insensitive and trims`() {
        assertEquals(ReleaseType.STABLE, ReleaseTypes.of("STABLE"))
        assertEquals(ReleaseType.BETA, ReleaseTypes.of("  Beta "))
        assertEquals(ReleaseType.ALPHA, ReleaseTypes.of("Alpha"))
    }

    @Test
    fun `unknown value fails fast`() {
        assertThrows(GradleException::class.java) { ReleaseTypes.of("release") }
    }

}

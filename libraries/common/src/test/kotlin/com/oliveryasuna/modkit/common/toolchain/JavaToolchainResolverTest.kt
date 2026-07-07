package com.oliveryasuna.modkit.common.toolchain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JavaToolchainResolverTest {

    @Test
    fun `minimumJdkFor returns the floor at and just below each boundary`() {
        // At each floor's lower bound it takes that floor; just below, it drops
        // to the next lower floor.
        assertEquals(25, JavaToolchainResolver.minimumJdkFor("26.1"))
        assertEquals(21, JavaToolchainResolver.minimumJdkFor("26.0"))   // below 26.1

        assertEquals(21, JavaToolchainResolver.minimumJdkFor("1.20.5"))
        assertEquals(17, JavaToolchainResolver.minimumJdkFor("1.20.4")) // below 1.20.5

        assertEquals(17, JavaToolchainResolver.minimumJdkFor("1.18"))
        assertEquals(16, JavaToolchainResolver.minimumJdkFor("1.17.9")) // below 1.18

        assertEquals(16, JavaToolchainResolver.minimumJdkFor("1.17"))
        assertEquals(8, JavaToolchainResolver.minimumJdkFor("1.16.5"))  // below 1.17

        assertEquals(8, JavaToolchainResolver.minimumJdkFor("1.12"))
    }

    @Test
    fun `minimumJdkFor falls back to 8 for versions below the oldest floor`() {
        assertEquals(8, JavaToolchainResolver.minimumJdkFor("1.7.10"))
    }

    @Test
    fun `resolveForTargets takes the max floor across the matrix`() {
        assertEquals(21, JavaToolchainResolver.resolveForTargets(listOf("1.20.4", "1.21.1")))
        assertEquals(25, JavaToolchainResolver.resolveForTargets(listOf("1.18", "26.1")))
        assertEquals(17, JavaToolchainResolver.resolveForTargets(listOf("1.18")))
    }

    @Test
    fun `resolveForTargets returns the newest floor for an empty matrix`() {
        assertEquals(25, JavaToolchainResolver.resolveForTargets(emptyList()))
    }

}

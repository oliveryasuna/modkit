package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ActiveLoaderTest {

    @Test
    fun `null or blank resolves to null`() {
        assertNull(ActiveLoader.resolve(null))
        assertNull(ActiveLoader.resolve(""))
        assertNull(ActiveLoader.resolve("   "))
    }

    @Test
    fun `fabric and neoforge resolve case-insensitively`() {
        assertEquals(McLoader.FABRIC, ActiveLoader.resolve("fabric"))
        assertEquals(McLoader.FABRIC, ActiveLoader.resolve("  Fabric "))
        assertEquals(McLoader.NEOFORGE, ActiveLoader.resolve("NeoForge"))
    }

    @Test
    fun `unknown value fails fast`() {
        assertThrows(GradleException::class.java) { ActiveLoader.resolve("bogus") }
    }

    @Test
    fun `parseable but unsupported loaders are rejected`() {
        // forge/quilt are valid McLoader entries but have no base wired yet.
        assertThrows(GradleException::class.java) { ActiveLoader.resolve("forge") }
        assertThrows(GradleException::class.java) { ActiveLoader.resolve("quilt") }
    }

}

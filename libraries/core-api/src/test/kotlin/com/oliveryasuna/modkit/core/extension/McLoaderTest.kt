package com.oliveryasuna.modkit.core.extension

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class McLoaderTest {

    @Test
    fun `null or blank yields null`() {
        assertNull(McLoader.fromProperty(null))
        assertNull(McLoader.fromProperty(""))
        assertNull(McLoader.fromProperty("   "))
    }

    @Test
    fun `known names resolve case-insensitively and trimmed`() {
        assertEquals(McLoader.FABRIC, McLoader.fromProperty("fabric"))
        assertEquals(McLoader.FABRIC, McLoader.fromProperty("FABRIC"))
        assertEquals(McLoader.NEOFORGE, McLoader.fromProperty(" NeoForge "))
    }

    @Test
    fun `unknown name throws`() {
        assertThrows(IllegalArgumentException::class.java) { McLoader.fromProperty("bogus") }
    }

}

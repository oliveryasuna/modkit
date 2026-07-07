package com.oliveryasuna.modkit.common.version

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SemVerTest {

    @Test
    fun `parse fills missing minor and patch with zero`() {
        assertEquals(SemVer(1, 0, 0), SemVer.parse("1"))
        assertEquals(SemVer(1, 21, 0), SemVer.parse("1.21"))
        assertEquals(SemVer(1, 20, 5), SemVer.parse("1.20.5"))
    }

    @Test
    fun `ordering is major then minor then patch`() {
        assertTrue(SemVer.parse("1.20.5") > SemVer.parse("1.20.4"))
        assertTrue(SemVer.parse("1.21") > SemVer.parse("1.20.5"))
        assertTrue(SemVer.parse("2.0") > SemVer.parse("1.99.99"))
        assertEquals(0, SemVer.parse("1.21").compareTo(SemVer.parse("1.21.0")))
    }

    @Test
    fun `toString round-trips the canonical form`() {
        assertEquals("1.20.5", SemVer(1, 20, 5).toString())
        assertEquals("1.21.0", SemVer.parse("1.21").toString())
    }

    @Test
    fun `equal values are usable as map keys`() {
        val map = mapOf(SemVer(1, 21, 0) to "a")
        assertEquals("a", map[SemVer.parse("1.21")])
    }

    @Test
    fun `parse rejects malformed and snapshot input`() {
        listOf(
            "",            // blank
            "   ",         // blank
            "24w14a",      // weekly snapshot
            "1.21-pre1",   // pre-release
            "1.20.5-rc1",  // release candidate
            "1.2.3.4",     // too many components
            "1.x",         // non-numeric
            "-1.2",        // negative
        ).forEach { bad ->
            assertThrows(IllegalArgumentException::class.java, { SemVer.parse(bad) }, "expected '$bad' to throw")
        }
    }

    @Test
    fun `parseOrNull returns null on malformed input and a value on valid input`() {
        assertNull(SemVer.parseOrNull("24w14a"))
        assertNull(SemVer.parseOrNull(""))
        assertNotNull(SemVer.parseOrNull("1.21"))
        assertEquals(SemVer(1, 20, 5), SemVer.parseOrNull("1.20.5"))
    }

}

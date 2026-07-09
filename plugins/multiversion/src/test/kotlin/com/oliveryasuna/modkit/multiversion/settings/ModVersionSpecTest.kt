package com.oliveryasuna.modkit.multiversion.settings

import com.oliveryasuna.modkit.core.extension.McLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ModVersionSpecTest {

    @Test
    fun `each version expands to one node per loader, named mc-loader`() {
        val spec = ModVersionSpec(":mods:modA")
        spec.version("1.21.1", McLoader.FABRIC, McLoader.NEOFORGE)
        spec.version("1.20.1", McLoader.FABRIC)

        assertEquals(
            listOf("1.21.1-fabric", "1.21.1-neoforge", "1.20.1-fabric"),
            spec.nodes.map { it.nodeId }
        )
        assertEquals(
            listOf(McLoader.FABRIC, McLoader.NEOFORGE, McLoader.FABRIC),
            spec.nodes.map { it.loader }
        )
        assertEquals("1.20.1", spec.nodes.last().minecraft)
    }

    @Test
    fun `a version with no loaders is rejected`() {
        val spec = ModVersionSpec(":mods:modA")
        assertThrows<IllegalArgumentException> { spec.version("1.21.1") }
    }

    @Test
    fun `duplicate version-loader nodes are rejected`() {
        val spec = ModVersionSpec(":mods:modA")
        spec.version("1.21.1", McLoader.FABRIC)
        assertThrows<IllegalArgumentException> { spec.version("1.21.1", McLoader.FABRIC) }
    }

    @Test
    fun `active defaults to null so the first node becomes vcsVersion`() {
        val spec = ModVersionSpec(":mods:modA")
        spec.version("1.21.1", McLoader.FABRIC)
        assertNull(spec.active)
    }
}

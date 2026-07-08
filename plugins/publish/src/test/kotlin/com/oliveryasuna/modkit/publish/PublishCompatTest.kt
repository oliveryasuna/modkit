package com.oliveryasuna.modkit.publish

import com.oliveryasuna.modkit.core.extension.McLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublishCompatTest {

    private fun target(version: String, enabled: Boolean, vararg loaders: McLoader) =
        PublishCompat.TargetView(version, enabled, loaders.toSet())

    @Test
    fun `null loader yields empty compatibility`() {
        val compat = PublishCompat.derive(listOf(target("1.21.1", true, McLoader.FABRIC)), null)
        assertTrue(compat.minecraftVersions.isEmpty())
        assertTrue(compat.modLoaders.isEmpty())
    }

    @Test
    fun `single target contributes its version and the active loader name`() {
        val compat = PublishCompat.derive(listOf(target("1.21.1", true, McLoader.NEOFORGE)), McLoader.NEOFORGE)
        assertEquals(listOf("1.21.1"), compat.minecraftVersions)
        assertEquals(listOf("neoforge"), compat.modLoaders)
    }

    @Test
    fun `multiple targets contribute distinct versions`() {
        val compat = PublishCompat.derive(
            listOf(
                target("1.21.1", true, McLoader.FABRIC),
                target("1.20.1", true, McLoader.FABRIC),
                target("1.21.1", true, McLoader.FABRIC)
            ),
            McLoader.FABRIC
        )
        assertEquals(listOf("1.21.1", "1.20.1"), compat.minecraftVersions)
        assertEquals(listOf("fabric"), compat.modLoaders)
    }

    @Test
    fun `targets not declaring the active loader are filtered out`() {
        val compat = PublishCompat.derive(
            listOf(
                target("1.21.1", true, McLoader.FABRIC),
                target("1.20.1", true, McLoader.NEOFORGE)
            ),
            McLoader.FABRIC
        )
        assertEquals(listOf("1.21.1"), compat.minecraftVersions)
    }

    @Test
    fun `disabled targets are excluded`() {
        val compat = PublishCompat.derive(
            listOf(
                target("1.21.1", false, McLoader.FABRIC),
                target("1.20.1", true, McLoader.FABRIC)
            ),
            McLoader.FABRIC
        )
        assertEquals(listOf("1.20.1"), compat.minecraftVersions)
    }

}

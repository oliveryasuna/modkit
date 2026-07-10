package com.oliveryasuna.modkit.scaffold

import com.oliveryasuna.modkit.core.extension.McLoader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ScaffoldPlanTest {

    private fun plan(
        modId: String? = "mymod",
        group: String = "com.example",
        versions: List<String> = listOf("1.21.1"),
        loaders: List<String> = listOf("fabric"),
        modules: List<String> = listOf("metadata", "mixins", "run"),
    ): ScaffoldPlan =
        ScaffoldPlan.of(modId, group, "1.0.0", versions, loaders, modules)

    @Test
    fun `single node selects the simple shape`() {
        assertEquals(ScaffoldShape.SIMPLE, plan().shape)
        assertEquals(1, plan().nodes.size)
    }

    @Test
    fun `multiple nodes select the multiversion shape`() {
        val p = plan(versions = listOf("1.21.1", "1.20.1"), loaders = listOf("fabric", "neoforge"))
        assertEquals(ScaffoldShape.MULTIVERSION, p.shape)
        assertEquals(4, p.nodes.size)
    }

    @Test
    fun `two loaders on one version is multiversion`() {
        val p = plan(loaders = listOf("fabric", "neoforge"))
        assertEquals(ScaffoldShape.MULTIVERSION, p.shape)
        assertEquals(2, p.nodes.size)
    }

    @Test
    fun `loaders map case-insensitively to McLoader`() {
        val p = plan(loaders = listOf("FABRIC", "NeoForge"))
        assertEquals(listOf(McLoader.FABRIC, McLoader.NEOFORGE), p.nodes.map { it.loader })
    }

    @Test
    fun `bad loader fails with a clear message`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            plan(loaders = listOf("forge"))
        }
        assertTrue(e.message!!.contains("forge"), e.message)
    }

    @Test
    fun `unparseable version fails with the offending value`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            plan(versions = listOf("1.21.x"))
        }
        assertTrue(e.message!!.contains("1.21.x"), e.message)
    }

    @Test
    fun `snapshot version is rejected`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            plan(versions = listOf("1.21-SNAPSHOT"))
        }
        assertTrue(e.message!!.contains("1.21-SNAPSHOT"), e.message)
    }

    @Test
    fun `blank modId is rejected`() {
        val e = assertThrows(IllegalArgumentException::class.java) { plan(modId = "   ") }
        assertTrue(e.message!!.contains("modId"), e.message)
    }

    @Test
    fun `null modId is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { plan(modId = null) }
    }

    @Test
    fun `unknown module fails with a clear message`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            plan(modules = listOf("metadata", "bogus"))
        }
        assertTrue(e.message!!.contains("bogus"), e.message)
    }

    @Test
    fun `modules map to their plugin ids`() {
        assertEquals("com.oliveryasuna.modkit.metadata", ScaffoldModule.METADATA.pluginId)
        assertEquals("com.oliveryasuna.modkit.mixins", ScaffoldModule.MIXINS.pluginId)
        assertEquals("com.oliveryasuna.modkit.run", ScaffoldModule.RUN.pluginId)
        assertEquals("com.oliveryasuna.modkit.publish", ScaffoldModule.PUBLISH.pluginId)
        assertEquals("com.oliveryasuna.modkit.ci", ScaffoldModule.CI.pluginId)
        assertEquals("com.oliveryasuna.modkit.dependencies", ScaffoldModule.DEPENDENCIES.pluginId)
        assertEquals("com.oliveryasuna.modkit.datagen", ScaffoldModule.DATAGEN.pluginId)
    }

    @Test
    fun `duplicate modules are deduped preserving order`() {
        val p = plan(modules = listOf("run", "metadata", "run"))
        assertEquals(listOf(ScaffoldModule.RUN, ScaffoldModule.METADATA), p.modules)
    }
}

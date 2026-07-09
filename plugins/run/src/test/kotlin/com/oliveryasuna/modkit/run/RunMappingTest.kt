package com.oliveryasuna.modkit.run

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RunMappingTest {

    private fun values(
        gameDir: String = "run/client",
        jvmArgs: List<String> = emptyList(),
        programArgs: List<String> = emptyList(),
        systemProperties: Map<String, String> = emptyMap(),
        environment: Map<String, String> = emptyMap(),
        auth: Boolean = false,
        enabled: Boolean = true
    ) = RunConfigValues(gameDir, jvmArgs, programArgs, systemProperties, environment, auth, enabled)

    @Test
    fun `system properties become dash-D vm args`() {
        val vmArgs = systemPropertiesToVmArgs(linkedMapOf("a" to "1", "b" to "2"))
        assertEquals(listOf("-Da=1", "-Db=2"), vmArgs)
    }

    @Test
    fun `loom vm args are jvm args followed by system properties`() {
        val mapping = mapRunConfigToLoom(
            "client",
            values(jvmArgs = listOf("-Xmx2G"), systemProperties = linkedMapOf("foo" to "bar"))
        )
        assertEquals(listOf("-Xmx2G", "-Dfoo=bar"), mapping.vmArgs)
    }

    @Test
    fun `loom run dir and program args pass through`() {
        val mapping = mapRunConfigToLoom(
            "server",
            values(gameDir = "run/server", programArgs = listOf("--nogui"))
        )
        assertEquals("run/server", mapping.runDir)
        assertEquals(listOf("--nogui"), mapping.programArgs)
    }

    @Test
    fun `loom mapping drops environment variables with a warning`() {
        val mapping = mapRunConfigToLoom("client", values(environment = mapOf("TOKEN" to "x")))
        assertTrue(mapping.warnings.any { it.contains("environment variables") }, mapping.warnings.toString())
    }

    @Test
    fun `loom mapping drops auth with a warning`() {
        val mapping = mapRunConfigToLoom("client", values(auth = true))
        assertTrue(mapping.warnings.any { it.contains("dev login") }, mapping.warnings.toString())
    }

    @Test
    fun `loom mapping has no warnings when only supported features are set`() {
        val mapping = mapRunConfigToLoom(
            "client",
            values(jvmArgs = listOf("-Xmx1G"), systemProperties = mapOf("a" to "1"), programArgs = listOf("--x"))
        )
        assertTrue(mapping.warnings.isEmpty(), mapping.warnings.toString())
    }

    @Test
    fun `hotswap report flags a standard jvm when jbr is preferred`() {
        val report = hotswapReport(vendor = "Oracle Corporation", vmName = "OpenJDK 64-Bit Server VM", preferJetBrainsRuntime = true)
        assertTrue(report.contains("Standard HotSpot"), report)
        assertTrue(report.contains("JetBrains Runtime"), report)
    }

    @Test
    fun `hotswap report recognizes a jetbrains runtime`() {
        val report = hotswapReport(vendor = "JetBrains s.r.o.", vmName = "OpenJDK 64-Bit Server VM", preferJetBrainsRuntime = true)
        assertTrue(report.contains("JetBrains Runtime detected"), report)
        assertTrue(report.contains("satisfied"), report)
    }

    @Test
    fun `hotswap report is quiet when preference is disabled`() {
        val report = hotswapReport(vendor = "Oracle Corporation", vmName = "OpenJDK 64-Bit Server VM", preferJetBrainsRuntime = false)
        assertTrue(report.contains("disabled"), report)
    }

}

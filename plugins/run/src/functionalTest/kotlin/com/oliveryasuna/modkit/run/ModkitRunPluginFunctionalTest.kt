package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.test.FunctionalTestBase
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * These deliberately do not apply Fabric Loom or ModDevGradle. Those backends
 * need a real Minecraft dependency resolved from the network, which would make
 * the suite slow and flaky. The reporting surface exercises the whole DSL end
 * to end (defaults, overrides, variants, and their snapshotting) without a
 * loader.
 */
class ModkitRunPluginFunctionalTest : FunctionalTestBase() {

    private fun runPlugin(modkitBody: String) {
        build(
            """
            plugins {
                id("com.oliveryasuna.modkit.run")
            }

            modkit {
                modId.set("mymod")
                version.set("1.0.0")
                $modkitBody
            }
            """.trimIndent()
        )
    }

    @Test
    fun `modkitRunInfo reports the default client and server runs`() {
        settings()
        runPlugin("")

        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(result.output.contains("Modkit run configurations:"), result.output)
        assertTrue(result.output.contains("client: enabled=true gameDir=run/client"), result.output)
        assertTrue(result.output.contains("server: enabled=true gameDir=run/server"), result.output)
    }

    @Test
    fun `modkitRunInfo shows data and gametest disabled by default`() {
        settings()
        runPlugin("")

        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(result.output.contains("data: enabled=false"), result.output)
        assertTrue(result.output.contains("gametest: enabled=false"), result.output)
    }

    @Test
    fun `modkitRunInfo reflects overridden run arguments`() {
        settings()
        runPlugin(
            """
            run {
                client {
                    gameDir("run/dev")
                    jvmArgs.add("-Xmx4G")
                    programArgs.add("--width=1280")
                    systemProperties.put("mixin.debug", "true")
                    environment.put("MOD_ENV", "dev")
                    auth.set(true)
                }
            }
            """.trimIndent()
        )

        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(result.output.contains("client: enabled=true gameDir=run/dev"), result.output)
        assertTrue(result.output.contains("jvmArgs=[-Xmx4G]"), result.output)
        assertTrue(result.output.contains("programArgs=[--width=1280]"), result.output)
        assertTrue(result.output.contains("systemProperties={mixin.debug=true}"), result.output)
        assertTrue(result.output.contains("environment={MOD_ENV=dev}"), result.output)
        assertTrue(result.output.contains("auth=true"), result.output)
    }

    @Test
    fun `modkitRunInfo enables a run when its config turns it on`() {
        settings()
        runPlugin(
            """
            run {
                data {
                    enabled.set(true)
                    gameDir("run/gen")
                }
            }
            """.trimIndent()
        )

        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(result.output.contains("data: enabled=true gameDir=run/gen"), result.output)
    }

    @Test
    fun `modkitRunInfo reports enhanced hot-swap when preference is enabled but JVM lacks it`() {
        settings()
        runPlugin("")

        val result = runner("modkitRunInfo", "-q").build()

        // The default is preferJetBrainsRuntime = true; the Gradle daemon runs
        // on a plain HotSpot JVM in CI, so the guidance is the "enabled but not
        // satisfied" branch.
        assertTrue(result.output.contains("hotswap.preferJetBrainsRuntime is enabled"), result.output)
    }

    @Test
    fun `modkitRunInfo drops the hot-swap request when the preference is disabled`() {
        settings()
        runPlugin(
            """
            run {
                hotswap {
                    preferJetBrainsRuntime.set(false)
                }
            }
            """.trimIndent()
        )

        val result = runner("modkitRunInfo", "-q").build()

        assertTrue(
            result.output.contains("hotswap.preferJetBrainsRuntime is disabled"),
            result.output
        )
    }

    @Test
    fun `modkitDoctor lists only enabled fixed runs`() {
        settings()
        runPlugin("")

        val result = runner("modkitDoctor", "-q").build()

        assertTrue(result.output.contains("[Runs]"), result.output)
        assertTrue(result.output.contains("client: gameDir=run/client"), result.output)
        assertTrue(result.output.contains("server: gameDir=run/server"), result.output)
        // data/gametest are disabled by default, so they are omitted.
        assertFalse(result.output.contains("data: gameDir="), result.output)
        assertFalse(result.output.contains("gametest: gameDir="), result.output)
    }

    @Test
    fun `modkitDoctor reports variants with their kinds and mod count`() {
        settings()
        runPlugin(
            """
            run {
                variants {
                    register("modMenu") {
                        appliesTo("client")
                        mods("maven.modrinth:modmenu:11.0.0")
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner("modkitDoctor", "-q").build()

        assertTrue(result.output.contains("variants:"), result.output)
        assertTrue(result.output.contains("modMenu -> [client] (1 mods)"), result.output)
    }

    @Test
    fun `a variant inherits mods from another via extends`() {
        settings()
        runPlugin(
            """
            run {
                variants {
                    register("base") {
                        appliesTo("client")
                        mods("maven.modrinth:modmenu:11.0.0")
                    }
                    register("extra") {
                        appliesTo("client")
                        extends("base")
                        mods("maven.modrinth:sodium:0.5.0")
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner("modkitDoctor", "-q").build()

        // extra carries its own mod plus the one it inherited from base.
        assertTrue(result.output.contains("extra -> [client] (2 mods)"), result.output)
    }

    @Test
    fun `modkitRunInfo is compatible with the configuration cache`() {
        settings()
        runPlugin("")

        val first = runner("modkitRunInfo", "--configuration-cache").build()
        assertEquals(
            TaskOutcome.SUCCESS,
            first.task(":modkitRunInfo")?.outcome,
            first.output
        )

        val second = runner("modkitRunInfo", "--configuration-cache").build()
        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    // TODO: Add tests with real Fabric Loom and MDG.
    //       This would end up taking a long time to run, so maybe we can cache
    //       them in CI?

}

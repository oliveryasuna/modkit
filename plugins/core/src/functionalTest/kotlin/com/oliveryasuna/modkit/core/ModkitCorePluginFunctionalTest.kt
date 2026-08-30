package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.test.FunctionalTestBase
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModkitCorePluginFunctionalTest : FunctionalTestBase() {

    @Test
    fun `modkitModel prints the resolved model`() {
        settings()
        build(
            """
                import com.oliveryasuna.modkit.core.extension.ModLoader

                plugins {
                    id("com.oliveryasuna.modkit.core")
                }

                modkit {
                    modId.set("mymod")
                    version.set("9.9.9")
                    minecraft("1.20.1") {
                        loaders.add(ModLoader.FABRIC)
                    }
                }
            """.trimIndent()
        )

        val result = runner("modkitModel", "-q").build()

        assertTrue(result.output.contains("modId:     mymod"), result.output)
        assertTrue(result.output.contains("version:   9.9.9"), result.output)
        assertTrue(result.output.contains("toolchain: 17"), result.output)
        assertTrue(result.output.contains("1.20.1 -> [FABRIC]"), result.output)
    }

    @Test
    fun `modkitValidateModel fails on an invalid modId`() {
        settings()
        build(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("invalid%")
            }
            """.trimIndent()
        )

        val result = runner("modkitValidateModel").buildAndFail()

        assertTrue(result.output.contains("must match ^[a-z][a-z0-9_-]{1,63}$"), result.output)
    }

    @Test
    fun `modkitValidateModel fails on an missing target matrix`() {
        settings()
        build(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
            }
            """.trimIndent()
        )

        val result = runner("modkitValidateModel").buildAndFail()

        assertTrue(result.output.contains("at least one target is required"), result.output)
    }

    @Test
    fun `modkitValidateModel fails on an empty target matrix`() {
        settings()
        build(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.20.1") {}
            }
            """.trimIndent()
        )

        val result = runner("modkitValidateModel").buildAndFail()

        assertTrue(result.output.contains("must declare at least one loader"), result.output)
    }

    @Test
    fun `check runs validation and reuses the configuration cache`() {
        settings()
        build(
            """
            plugins {
                id("base")
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                minecraft("1.20.1") { loaders.add(com.oliveryasuna.modkit.core.extension.ModLoader.NEOFORGE) }
            }
            """.trimIndent()
        )

        val first = runner("check", "--configuration-cache").build()
        assertEquals(
            TaskOutcome.SUCCESS,
            first.task(":modkitValidateModel")?.outcome,
            first.output
        )

        val second = runner("check", "--configuration-cache").build()
        assertTrue(second.output.contains("Reusing configuration cache."), second.output)
    }

    @Test
    fun `modkitDoctor reports the model section and flags missing targets`() {
        settings()
        build(
            """
            plugins {
                id("com.oliveryasuna.modkit.core")
            }

            modkit {
                modId.set("mymod")
                version.set("1.2.3")
                // No targets declared → a problem is surfaced.
            }
            """.trimIndent()
        )

        val result = runner("modkitDoctor", "-q", "--configuration-cache").build()

        assertTrue(result.output.contains("[Model]"), result.output)
        assertTrue(result.output.contains("modId:     mymod"), result.output)
        assertTrue(result.output.contains("[Problems]"), result.output)
        assertTrue(result.output.contains("No Minecraft targets declared"), result.output)
    }

}

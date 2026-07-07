package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.ModkitModelValidator.TargetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModkitModelValidatorTest {

    private val oneGoodTarget = listOf(TargetView("1.21.1", hasLoaders = true))

    @Test
    fun `valid model reports no errors`() {
        assertTrue(ModkitModelValidator.validate("mymod", oneGoodTarget).isEmpty())
    }

    @Test
    fun `null or blank modId is rejected`() {
        assertTrue(ModkitModelValidator.validate(null, oneGoodTarget).any { it.contains("modId is required") })
        assertTrue(ModkitModelValidator.validate("   ", oneGoodTarget).any { it.contains("modId is required") })
    }

    @Test
    fun `modId not matching the pattern is rejected`() {
        // Leading digit, uppercase, and single-char all violate the pattern.
        listOf("1mod", "MyMod", "m").forEach { bad ->
            assertTrue(
                ModkitModelValidator.validate(bad, oneGoodTarget).any { it.contains("must match") },
                "expected '$bad' to be rejected"
            )
        }
    }

    @Test
    fun `empty targets is rejected`() {
        assertTrue(
            ModkitModelValidator.validate("mymod", emptyList()).any { it.contains("at least one target") }
        )
    }

    @Test
    fun `target without loaders is rejected`() {
        val errors = ModkitModelValidator.validate("mymod", listOf(TargetView("1.21.1", hasLoaders = false)))
        assertEquals(1, errors.size)
        assertTrue(errors.single().contains("must declare at least one loader"))
    }

    @Test
    fun `multiple problems are all reported`() {
        val errors = ModkitModelValidator.validate(
            modId = "",
            targets = listOf(TargetView("1.21.1", hasLoaders = false))
        )
        assertEquals(2, errors.size)
    }

}

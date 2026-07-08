package com.oliveryasuna.modkit.loaders

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AwToAtTest {

    private fun aw(vararg lines: String): String =
        (listOf("accessWidener v2 named") + lines).joinToString("\n", postfix = "\n")

    private fun convert(vararg lines: String): List<String> =
        AwToAt.convert(listOf(aw(*lines))).lines().filter { it.isNotBlank() && !it.startsWith("#") }

    @Test
    fun `accessible widens to public`() {
        assertEquals(listOf("public net.minecraft.Foo"), convert("accessible class net/minecraft/Foo"))
    }

    @Test
    fun `extendable class strips final`() {
        assertEquals(listOf("public-f net.minecraft.Foo"), convert("extendable class net/minecraft/Foo"))
    }

    @Test
    fun `accessible method keeps its descriptor`() {
        assertEquals(
            listOf("public net.minecraft.Foo bar()V"),
            convert("accessible method net/minecraft/Foo bar ()V")
        )
    }

    @Test
    fun `accessible field drops its descriptor`() {
        assertEquals(
            listOf("public net.minecraft.Foo baz"),
            convert("accessible field net/minecraft/Foo baz I")
        )
    }

    @Test
    fun `mutable field strips final`() {
        assertEquals(
            listOf("public-f net.minecraft.Foo baz"),
            convert("mutable field net/minecraft/Foo baz I")
        )
    }

    @Test
    fun `accessible and mutable on the same field merge into one stripped line`() {
        val lines = convert(
            "accessible field net/minecraft/Foo baz I",
            "mutable field net/minecraft/Foo baz I"
        )
        assertEquals(listOf("public-f net.minecraft.Foo baz"), lines)
    }

    @Test
    fun `an empty widener produces a valid header-only transformer`() {
        val out = AwToAt.convert(listOf(aw()))
        assertTrue(out.startsWith("#"), out)
        assertFalse(out.lines().any { it.startsWith("public") }, out)
    }

}

package com.oliveryasuna.modkit.mixins

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class MixinScannerTest {

    private fun mixinClass(internalName: String, target: String): ByteArray {
        val cw = ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null)
        val annotation = cw.visitAnnotation(MixinScanner.MIXIN_DESCRIPTOR, false)
        val targets = annotation.visitArray("targets")
        targets.visit(null, target)
        targets.visitEnd()
        annotation.visitEnd()
        cw.visitEnd()
        return cw.toByteArray()
    }

    @Test
    fun `scans the mixin annotation and its string targets`() {
        val refs = MixinScanner.scan(mixinClass("com/example/BogusMixin", "does/not/Exist"))

        assertEquals(1, refs.size)
        assertEquals("com/example/BogusMixin", refs[0].className)
        assertTrue(refs[0].targets.contains("does/not/Exist"), refs[0].targets.toString())
    }

    @Test
    fun `dotted targets are normalized to internal names`() {
        val refs = MixinScanner.scan(mixinClass("com/example/DottedMixin", "net.minecraft.Foo"))

        assertTrue(refs[0].targets.contains("net/minecraft/Foo"), refs[0].targets.toString())
    }

    @Test
    fun `classes without the mixin annotation yield no refs`() {
        val cw = ClassWriter(0)
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "com/example/Plain", null, "java/lang/Object", null)
        cw.visitEnd()

        assertTrue(MixinScanner.scan(cw.toByteArray()).isEmpty())
    }

}

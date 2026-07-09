package com.oliveryasuna.modkit.mixins

import org.objectweb.asm.*

/**
 * A `@Mixin`-annotated class and the target classes it references, both the
 * `value` class literals and the `targets` string names, normalized to JVM
 * internal names (`a/b/C`).
 */
internal data class MixinRef(
    val className: String,
    val targets: List<String>
)

/**
 * Reads `@Mixin` annotations off compiled bytecode with ASM. Kept free of
 * Gradle types so the parse is unit-testable against synthetic class bytes.
 */
internal object MixinScanner {

    const val MIXIN_DESCRIPTOR: String = "Lorg/spongepowered/asm/mixin/Mixin;"

    /**
     * Parses [classBytes], returning one [MixinRef] per `@Mixin` found (usually
     * 0 or 1).
     */
    fun scan(classBytes: ByteArray): List<MixinRef> {
        val refs = ArrayList<MixinRef>()
        val holder = arrayOf<String>("")

        val visitor = object : ClassVisitor(Opcodes.ASM9) {
            override fun visit(
                version: Int,
                access: Int,
                name: String,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
            ) {
                holder[0] = name
            }

            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                if(descriptor != MIXIN_DESCRIPTOR) return null

                val targets = ArrayList<String>()
                return object : AnnotationVisitor(Opcodes.ASM9) {
                    override fun visitArray(name: String?): AnnotationVisitor? =
                        when(name) {
                            "value" -> object : AnnotationVisitor(Opcodes.ASM9) {
                                override fun visit(elementName: String?, value: Any?) {
                                    if(value is Type) targets.add(value.internalName)
                                }
                            }

                            "targets" -> object : AnnotationVisitor(Opcodes.ASM9) {
                                override fun visit(elementName: String?, value: Any?) {
                                    if(value is String) targets.add(normalize(value))
                                }
                            }

                            else -> null
                        }

                    override fun visitEnd() {
                        refs.add(MixinRef(holder[0], targets))
                    }
                }
            }
        }

        ClassReader(classBytes).accept(
            visitor,
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )
        return refs
    }

    /** Converts a dotted or slashed class name to a JVM internal name. */
    fun normalize(className: String): String =
        className.replace('.', '/')

}

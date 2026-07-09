package com.oliveryasuna.modkit.mixins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.File
import java.util.zip.ZipFile

/**
 * Verifies that the target classes referenced by the project's compiled
 * `@Mixin` classes can be resolved. A no-op success when linting is disabled.
 * All inputs are plain snapshots so the task is configuration-cache safe; the
 * bytecode scan runs entirely against the declared file inputs.
 */
@CacheableTask
internal abstract class LintMixinsTask : DefaultTask() {

    @get:Input
    abstract val lintEnabled: Property<Boolean>

    @get:Input
    abstract val checkTargetsExist: Property<Boolean>

    /** Base packages of the registered configs; empty means scan everything. */
    @get:Input
    abstract val packages: SetProperty<String>

    /**
     * Compiled classes of the main source set — the scan's `@Mixin` sources.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesDirs: ConfigurableFileCollection

    /** Main compile classpath — where declared target classes must resolve. */
    @get:Classpath
    abstract val compileClasspath: ConfigurableFileCollection

    @TaskAction
    fun lint() {
        if(!lintEnabled.get()) return

        val prefixes = packages.get().map { MixinScanner.normalize(it) }
        val available = collectAvailableClasses()

        val missing = LinkedHashMap<String, MutableSet<String>>()
        for(classFile in classFilesToScan(prefixes)) {
            for(ref in MixinScanner.scan(classFile.readBytes())) {
                for(target in ref.targets) {
                    if(target !in available) {
                        missing.getOrPut(ref.className.replace('/', '.')) { LinkedHashSet() }
                            .add(target.replace('/', '.'))
                    }
                }
            }
        }

        if(checkTargetsExist.get() && missing.isNotEmpty()) {
            val details = missing.entries.joinToString("\n") { (owner, targets) ->
                "  $owner -> ${targets.joinToString(", ")}"
            }
            throw GradleException(
                "Mixin lint failed: the following @Mixin classes reference target classes that " +
                "cannot be found on the compile classpath:\n$details"
            )
        }
    }

    /**
     * Compiled `.class` files under the configured packages (all, if none
     * configured).
     */
    private fun classFilesToScan(prefixes: List<String>): Sequence<File> =
        classesDirs.files.asSequence()
            .filter { it.isDirectory }
            .flatMap { root -> classFilesUnder(root, prefixes) }

    private fun classFilesUnder(root: File, prefixes: List<String>): Sequence<File> =
        root.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".class") }
            .filter { file ->
                if(prefixes.isEmpty()) return@filter true
                val internal = root.toPath().relativize(file.toPath()).toString()
                    .replace(File.separatorChar, '/')
                    .removeSuffix(".class")
                prefixes.any { internal.startsWith("$it/") || internal.substringBeforeLast('/', "") == it }
            }

    /**
     * Every class internal name present in the compiled output and on the
     * classpath.
     */
    private fun collectAvailableClasses(): Set<String> {
        val names = HashSet<String>()
        for(dir in classesDirs.files) {
            if(dir.isDirectory) addClassesFromDir(dir, names)
        }
        for(entry in compileClasspath.files) {
            when {
                entry.isDirectory -> addClassesFromDir(entry, names)
                entry.isFile && entry.name.endsWith(".jar") -> addClassesFromJar(entry, names)
            }
        }
        return names
    }

    private fun addClassesFromDir(root: File, into: MutableSet<String>) {
        root.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".class") }
            .forEach { file ->
                val internal = root.toPath().relativize(file.toPath()).toString()
                    .replace(File.separatorChar, '/')
                    .removeSuffix(".class")
                into.add(internal)
            }
    }

    private fun addClassesFromJar(jar: File, into: MutableSet<String>) {
        ZipFile(jar).use { zip ->
            val entries = zip.entries()
            while(entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if(!entry.isDirectory && entry.name.endsWith(".class")) {
                    into.add(entry.name.removeSuffix(".class"))
                }
            }
        }
    }

}

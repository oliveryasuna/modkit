package com.oliveryasuna.modkit.scaffold

import com.oliveryasuna.modkit.scaffold.render.ScaffoldRenderer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Generates a working Modkit consumer project — settings script, build script,
 * `gradle.properties`, and example sources — for the requested loader/version
 * matrix. A single (version, loader) node yields the plain project shape; more
 * than one yields the Stonecutter multiversion root layout.
 *
 * Not cacheable: it writes into a user tree, and refuses to overwrite existing
 * files unless [force] is set.
 */
@DisableCachingByDefault(because = "Generates a user project tree; nothing to cache and it must re-check for existing files every run.")
public abstract class ModkitInitTask : DefaultTask() {

    @get:Input
    @get:Optional
    public abstract val modId: Property<String>

    /**
     * The Maven group of the generated mod (the `-Pgroup` flag; emitted as
     * `group.set(...)` in the generated build script).
     *
     * Named `modGroup` rather than `group` because a Gradle [DefaultTask]
     * already declares `getGroup(): String` (the task's lifecycle group); a
     * managed `Property<String>` getter of the same name is a JVM signature
     * clash the task-class generator rejects. The user-facing flag and the
     * generated DSL are unaffected.
     */
    @get:Input
    @get:Optional
    public abstract val modGroup: Property<String>

    @get:Input
    @get:Optional
    public abstract val versions: ListProperty<String>

    @get:Input
    @get:Optional
    public abstract val loaders: ListProperty<String>

    @get:Input
    @get:Optional
    public abstract val modules: ListProperty<String>

    @get:Input
    @get:Optional
    public abstract val force: Property<Boolean>

    @get:OutputDirectory
    public abstract val targetDir: DirectoryProperty

    @TaskAction
    public fun generate() {
        // An unset (or empty) list falls back to the default set — a bare
        // `modkitInit -PmodId=x` produces a Fabric 1.21.1 project.
        fun listOr(prop: ListProperty<String>, default: List<String>): List<String> =
            prop.getOrElse(emptyList()).ifEmpty { default }

        val plan = try {
            ScaffoldPlan.of(
                modId = modId.orNull,
                group = modGroup.getOrElse("com.example").ifBlank { "com.example" },
                version = "1.0.0",
                versions = listOr(versions, listOf("1.21.1")),
                loaders = listOr(loaders, listOf("fabric")),
                modules = listOr(modules, listOf("metadata", "mixins", "run")),
            )
        } catch(e: IllegalArgumentException) {
            throw GradleException(e.message ?: "Invalid scaffold inputs", e)
        }

        val root = targetDir.get().asFile
        val overwrite = force.getOrElse(false)
        val files = ScaffoldRenderer.render(plan)

        // Refuse the whole operation if any target file already exists, before
        // writing anything — so a rejected run never leaves a half-written tree.
        if(!overwrite) {
            val clashes = files
                .map { root.resolve(it.path) }
                .filter { it.exists() }
            if(clashes.isNotEmpty()) {
                throw GradleException("Refusing to overwrite existing files (pass -Pforce to overwrite):" + clashes.joinToString("") { "\n  " + it.relativeTo(root).path })
            }
        }

        files.forEach { file ->
            val target = root.resolve(file.path)
            target.parentFile.mkdirs()
            target.writeText(file.content)
        }

        logger.lifecycle("Modkit scaffold generated '${plan.modId}' (${plan.shape.name.lowercase()} shape, ${plan.nodes.size} node(s)) into ${root.absolutePath}")
    }
}

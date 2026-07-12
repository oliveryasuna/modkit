package com.oliveryasuna.modkit.mixins

import com.oliveryasuna.modkit.mixins.extension.MixinsSpec
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * Registers hand-authored mixin configs. This plugin does not generate the
 * `<name>.mixins.json` files — the user authors those. Its job is to provide
 * the refmap convention, publish each registered config's file name to the
 * shared manifest registry (so `metadata` lists it), and optionally lint the
 * compiled `@Mixin` classes.
 */
public class ModkitMixinsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `mixins` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `mixins` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val mixins = modkit.registerBlock("mixins", MixinsSpec::class.java)

        // Conventions.
        mixins.refmap.convention(modkit.modId.map { "$it-refmap.json" })
        mixins.configs.all { it.environment.convention("*") }
        mixins.lint.enabled.convention(false)
        mixins.lint.checkTargetsExist.convention(true)

        // Publish each registered config's file name to the shared registry so
        // `metadata` folds it into the mod manifests. Fires per element,
        // including configs registered later.
        mixins.configs.all { config ->
            project.modkitManifestContributions().mixinConfigs.add("${config.name}.mixins.json")
        }

        registerLint(project, mixins)
    }

    private fun registerLint(project: Project, mixins: MixinsSpec) {
        val lint = project.tasks.register("lintMixins", LintMixinsTask::class.java) { task ->
            task.group = "verification"
            task.description = "Verifies that target classes referenced by @Mixin classes can be resolved."

            task.lintEnabled.set(mixins.lint.enabled)
            task.checkTargetsExist.set(mixins.lint.checkTargetsExist)
            task.packages.set(project.provider { mixins.configs.mapNotNull { it.pkg.orNull }.toSet() })
        }

        // The `@Mixin` sources and classpath only exist once a Java model does.
        // Scan the common source set (`main` by default, or whatever
        // `modkit.commonSourceSet` names) — that is where the mod's mixin
        // classes live.
        val commonSourceSet = project.commonSourceSet()
        project.pluginManager.withPlugin("java-base") {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val common = sourceSets.getByName(commonSourceSet)
            lint.configure { task ->
                task.classesDirs.from(common.output.classesDirs)
                task.compileClasspath.from(common.compileClasspath)
            }
        }

        project.wireIntoCheck(lint)
    }

}

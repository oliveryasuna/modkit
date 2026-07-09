package com.oliveryasuna.modkit.multiversion

import com.oliveryasuna.modkit.multiversion.extension.MultiversionRuntime
import com.oliveryasuna.modkit.multiversion.extension.MultiversionSpec
import com.oliveryasuna.modkit.multiversion.extension.VersionOverride
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Project-side plugin, applied per generated node via the shared central build
 * script. Exposes `modkit { multiversion { } }` and delegates
 * `onVersion(range)` to Stonecutter's version evaluator for the current node.
 * On non-node projects (no Stonecutter extension) the block is inert.
 */
public class ModkitMultiversionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val modkit = project.applyModkitCore()
        val multiversion = modkit.registerBlock("multiversion", MultiversionSpec::class.java)

        // Present only on Stonecutter node projects; absent on the mod parent /
        // a plain single-version project -> leave the block inert.
        val stonecutter = project.extensions.findByType(StonecutterBuildExtension::class.java) ?: return
        multiversion.runtime = ProjectMultiversionRuntime(project, stonecutter)
    }

    private class ProjectMultiversionRuntime(
        private val project: Project,
        private val stonecutter: StonecutterBuildExtension
    ) : MultiversionRuntime {

        override fun matches(range: String): Boolean =
            stonecutter.eval(stonecutter.current.version, range)

        override fun newOverride(): VersionOverride =
            project.objects.newInstance(VersionOverride::class.java)

        override fun apply(override: VersionOverride) {
            override.properties.forEach { (key, value) ->
                project.extensions.extraProperties.set(key, value)
            }
            // Route extra deps to the `mod` bucket when the `dependencies`
            // plugin is present (so they are remapped/nested correctly), else
            // plain `implementation`.
            val configuration = if(project.configurations.findByName("mod") != null) "mod" else "implementation"
            override.dependencies.getOrElse(emptyMap()).forEach { (coordinate, version) ->
                project.dependencies.add(configuration, "$coordinate:$version")
            }
        }
    }
}

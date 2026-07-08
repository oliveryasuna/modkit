package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

public class ModkitLoadersPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `loaders` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `loaders` block as its ExtensionAware child.
        project.pluginManager.apply("com.oliveryasuna.modkit.core")

        val modkit = project.extensions.getByType(ModkitExtension::class.java)
        val loaders = (modkit as ExtensionAware).extensions
            .create("loaders", LoadersSpec::class.java)

        loaders.mappings.scheme.convention(MappingsScheme.MOJMAP)

        // Choose the base eagerly from `modkit.loader` — the model DSL has not
        // run yet, so the property is the only signal available at this point.
        val activeLoader = ActiveLoader.resolve(
            project.providers.gradleProperty(ActiveLoader.PROPERTY).orNull
        )

        registerDiagnostics(project, modkit, loaders, activeLoader)

        // Apply and configure the base for the active loader. Absent
        // property -> no base (diagnostics/model still work); NeoForge wiring
        // lands next.
        when(activeLoader) {
            McLoader.FABRIC -> configureFabric(project, modkit, loaders)
            McLoader.NEOFORGE -> configureNeoForge(project, modkit, loaders)
            null -> Unit
        }
    }

    private fun registerDiagnostics(
        project: Project,
        modkit: ModkitExtension,
        loaders: LoadersSpec,
        activeLoader: McLoader?
    ) {
        project.tasks.register("modkitLoaderInfo") { task ->
            task.group = "modkit"
            task.description = "Prints the resolved loader configuration (mappings, versions, targets)."

            // Capture provider/snapshot values at configuration time for
            // configuration-cache compatibility.
            val scheme = loaders.mappings.scheme
            val parchment = loaders.mappings.parchment
            val fabricLoader = loaders.fabric.loaderVersion
            val fabricApi = loaders.fabric.apiVersion
            val neoforge = loaders.neoforge.version
            val targets = modkit.targets.map { target -> "${target.minecraftVersion} -> ${target.loaders.get()}" }

            task.doLast {
                println("loader:    ${activeLoader?.name ?: "<not set> (set -P${ActiveLoader.PROPERTY}=fabric|neoforge)"}")
                println("mappings:  ${scheme.orNull}")
                println("parchment: ${parchment.orNull}")
                println("fabric:    loader=${fabricLoader.orNull} api=${fabricApi.orNull}")
                println("neoforge:  ${neoforge.orNull}")
                println("targets:")
                targets.forEach { println("  $it") }
            }
        }
    }

}

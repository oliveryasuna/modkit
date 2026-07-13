package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ModkitLoadersPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `loaders` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `loaders` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val loaders = modkit.registerBlock("loaders", LoadersSpec::class.java)

        loaders.mappings.scheme.convention(MappingsScheme.MOJMAP)

        // Choose the base eagerly from `modkit.loader` — the model DSL has not
        // run yet, so the property is the only signal available at this point.
        val activeLoader = project.activeLoader()

        registerDiagnostics(project, modkit, loaders, activeLoader)
        publishDiagnostics(project, loaders, activeLoader)

        // Split-client is also read eagerly: creating the client source set is
        // a structural change the base must make before Loom finalizes its
        // config, which the late model cannot drive.
        val splitClient = project.providers.gradleProperty(SPLIT_CLIENT_PROPERTY)
            .map { it.toBoolean() }
            .getOrElse(false)

        // The common source set the base binds the mod to. Read eagerly for the
        // same reason as splitClient (structural, pre-Loom-finalize).
        val commonSourceSet = project.commonSourceSet()

        // A non-`main` common source set combined with split-client is not
        // supported: Loom's `splitEnvironmentSourceSets()` splits `main`
        // specifically, so the two settings would disagree about which set is
        // "common". Fail clearly rather than produce a broken split.
        if(commonSourceSet != DEFAULT_COMMON_SOURCE_SET && splitClient) {
            throw GradleException(
                "modkit.commonSourceSet = '$commonSourceSet' cannot be combined with modkit.splitClient in v1 " +
                "(split-client is anchored to the 'main' source set). Use one or the other."
            )
        }

        // Apply and configure the base for the active loader. Absent
        // property -> no base (diagnostics/model still work).
        when(activeLoader) {
            McLoader.FABRIC -> configureFabric(project, modkit, loaders, splitClient, commonSourceSet)
            McLoader.NEOFORGE -> configureNeoForge(project, modkit, loaders, splitClient, commonSourceSet)
            null -> Unit
        }
    }

    private companion object {

        private const val SPLIT_CLIENT_PROPERTY: String = "modkit.splitClient"

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
                println("loader:    ${activeLoader?.name ?: "<not set> (set -P${McLoader.PROPERTY}=fabric|neoforge)"}")
                println("mappings:  ${scheme.orNull}")
                println("parchment: ${parchment.orNull}")
                println("fabric:    loader=${fabricLoader.orNull} api=${fabricApi.orNull}")
                println("neoforge:  ${neoforge.orNull}")
                println("targets:")
                targets.forEach { println("  $it") }
            }
        }
    }

    /**
     * Publishes the loader's `modkitDoctor` section + problems (no cross-plugin
     * dep).
     */
    private fun publishDiagnostics(
        project: Project,
        loaders: LoadersSpec,
        activeLoader: McLoader?
    ) {
        val diagnostics = project.modkitDiagnostics()

        val scheme = loaders.mappings.scheme
        val parchment = loaders.mappings.parchment
        val fabricLoader = loaders.fabric.loaderVersion
        val fabricApi = loaders.fabric.apiVersion
        val neoforge = loaders.neoforge.version

        diagnostics.sections.put(
            "Loader",
            project.provider {
                listOf(
                    "active:    ${activeLoader?.name ?: "<not set>"}",
                    "mappings:  ${scheme.orNull}${parchment.orNull?.let { " + parchment $it" } ?: ""}",
                    "fabric:    loader=${fabricLoader.orNull ?: "-"} api=${fabricApi.orNull ?: "-"}",
                    "neoforge:  ${neoforge.orNull ?: "-"}"
                )
            }
        )

        if(activeLoader == null) {
            diagnostics.problems.add(
                "No active loader — set -P${McLoader.PROPERTY}=fabric|neoforge; no mod will build."
            )
        }
    }

}

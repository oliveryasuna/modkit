package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Applies and configures ModDevGradle for a NEOFORGE target, mapping the Modkit
 * model onto MDG. Kept internal — no MDG types leak into loaders' public API.
 *
 * Unlike Loom, MDG's `NeoForgeExtension.setVersion` takes a plain String that
 * must be set before MDG configures its tasks — but the version arrives from
 * the `modkit` DSL, which runs after this plugin applies. So MDG application is
 * deferred to `afterEvaluate`, once the model and loaders DSL are populated.
 */
internal fun configureNeoForge(
    project: Project,
    modkit: ModkitExtension,
    loaders: LoadersSpec
) {
    // Transpile access wideners to a NeoForge access transformer (write-once).
    // Registered eagerly so it exists regardless of the afterEvaluate path.
    val generateAccessTransformer = project.tasks.register(
        "generateAccessTransformer",
        GenerateAccessTransformerTask::class.java
    ) { task ->
        task.group = "modkit"
        task.description = "Generates a NeoForge access transformer from the project's access wideners."
        task.accessWideners.from(loaders.accessWideners)
        task.accessTransformer.set(project.layout.buildDirectory.file("modkit/accesstransformer.cfg"))
    }

    project.afterEvaluate {
        val neoforge = modkit.targets.filter { target ->
            target.enabled.get() && McLoader.NEOFORGE in target.loaders.get()
        }
        require(neoforge.isNotEmpty()) {
            "modkit.loader=neoforge but no enabled target declares the neoforge loader."
        }
        require(neoforge.size == 1) {
            "loaders builds one variant per project, but ${neoforge.size} enabled targets declare " +
            "neoforge (${neoforge.joinToString { it.minecraftVersion }}). Use multiversion or declare one."
        }

        if(loaders.mappings.scheme.get() == MappingsScheme.YARN) {
            throw GradleException(
                "Yarn mappings are not supported on NeoForge (mojmap-native). Use scheme = MOJMAP."
            )
        }

        val version = loaders.neoforge.version.orNull
                      ?: throw GradleException("modkit.loader=neoforge requires modkit.loaders.neoforge.version to be set.")

        project.pluginManager.apply("net.neoforged.moddev")
        project.addParchmentRepository()
        val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)
        neoForge.setVersion(version)

        // Feed the generated AT to MDG (task output → carries the dependency).
        neoForge.accessTransformers.from(generateAccessTransformer.flatMap { it.accessTransformer })

        // Layer parchment when a version is present (NeoForge is mojmap-native,
        // so parchment applies directly).
        val parchmentVersion = loaders.mappings.parchment.orNull
        if(parchmentVersion != null) {
            neoForge.parchment.minecraftVersion.set(neoforge.single().minecraftVersion)
            neoForge.parchment.mappingsVersion.set(parchmentVersion)
        }
    }
}

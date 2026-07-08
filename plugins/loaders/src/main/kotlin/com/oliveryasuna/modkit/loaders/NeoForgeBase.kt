package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
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

        val version = loaders.neoforge.version.orNull
                      ?: throw GradleException("modkit.loader=neoforge requires modkit.loaders.neoforge.version to be set.")

        project.pluginManager.apply("net.neoforged.moddev")
        project.extensions.getByType(NeoForgeExtension::class.java).setVersion(version)
    }
}

package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import com.oliveryasuna.modkit.plugin.activeLoader
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Puts Fabric Loom and ModDevGradle behind one `modkit { loaders { } }` block.
 *
 * `apply()` stays small on purpose: attach the block, read the few structural
 * bits that have to be settled eagerly, then hand off to the base for whichever
 * loader is active. The interesting work lives in
 * [FabricBase] / [NeoForgeBase]; the upstream tooling never surfaces in this
 * plugin's public API.
 */
public class ModkitLoadersPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val model = project.applyModkitCore()
        val loaders = model.registerBlock(LOADERS_BLOCK, LoadersSpec::class.java)

        loaders.mappings.scheme.convention(MappingsScheme.MOJMAP)

        // The base is chosen from `modkit.loader`, read eagerly: the model DSL
        // has not run yet, so the property is the only signal available here.
        val activeLoader = project.activeLoader()

        LoaderDiagnostics.install(project, model, loaders, activeLoader)

        // Reads and validates the eager source-set decisions. Runs regardless
        // of the active loader, so an impossible combination fails even with no
        // base.
        val layout = SourceLayout.read(project)

        // No active loader still leaves diagnostics and the model working; only
        // an actuallb uild needs a base.
        activeLoader?.let { loader ->
            LoaderBase.forLoader(loader).install(LoaderContext(project, model, loaders, layout))
        }
    }

    private companion object {

        const val LOADERS_BLOCK: String = "loaders"

    }

}

package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.plugin.PluginContext
import org.gradle.api.Project

/**
 * A [PluginContext] carrying the extra bits a [LoaderBase] needs: the
 * `loaders`, DSL block, and the resolved [SourceLayout]. The project and model
 * come from the base class.
 */
internal class LoaderContext(
    project: Project,
    model: ModkitExtension,
    val loaders: LoadersSpec,
    val layout: SourceLayout
) : PluginContext(project, model)

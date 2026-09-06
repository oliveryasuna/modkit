package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import com.oliveryasuna.modkit.plugin.PluginContext
import org.gradle.api.Project

internal class MetadataContext(
    project: Project,
    model: ModkitExtension,
    val metadata: MetadataSpec,
    val activeLoader: ModLoader?,
    val commonSourceSet: String
) : PluginContext(project, model)

package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.PluginContext
import com.oliveryasuna.modkit.run.extension.RunSpec
import org.gradle.api.Project

internal class RunContext(
    project: Project,
    model: ModkitExtension,
    val run: RunSpec
) : PluginContext(project, model)

package com.oliveryasuna.modkit.plugin

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import org.gradle.api.Project

public open class PluginContext(
    public val project: Project,
    public val model: ModkitExtension
)

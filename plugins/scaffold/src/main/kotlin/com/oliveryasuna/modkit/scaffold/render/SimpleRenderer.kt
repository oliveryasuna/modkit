package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.scaffold.ScaffoldPlan

/**
 * Renders the single-node (non-Stonecutter) project shape: a plain
 * `settings.gradle.kts` + `gradle.properties` (single `modkit.loader`) +
 * `build.gradle.kts` with a populated `modkit { }`, plus the shared example
 * sources. Deterministic string builders — no live Gradle providers.
 */
internal object SimpleRenderer {

    fun render(plan: ScaffoldPlan): List<GeneratedFile> {
        val node = plan.nodes.single()
        val files = mutableListOf<GeneratedFile>()

        files += GeneratedFile("settings.gradle.kts", settings(plan))
        files += GeneratedFile("gradle.properties", gradleProperties(node.loader))
        files += GeneratedFile("build.gradle.kts", build(plan))
        files += SourceRenderer.render(plan)

        return files
    }

    private fun settings(plan: ScaffoldPlan): String = buildString {
        appendLine("pluginManagement {")
        appendLine("    repositories {")
        appendLine("        gradlePluginPortal()")
        appendLine("        mavenCentral()")
        // The loader plugins pull in Fabric Loom / ModDevGradle onto the
        // buildscript classpath; those live on the loader mavens, not the
        // Plugin Portal, so they must be reachable during plugin resolution.
        appendLine("        maven(\"https://maven.fabricmc.net/\")")
        appendLine("        maven(\"https://maven.neoforged.net/releases/\")")
        appendLine("    }")
        appendLine("}")
        appendLine()
        appendLine("plugins {")
        appendLine("    id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"1.0.0\"")
        appendLine("}")
        appendLine()
        appendLine("dependencyResolutionManagement {")
        appendLine("    repositories {")
        appendLine("        mavenCentral()")
        appendLine("        maven(\"https://maven.fabricmc.net/\")")
        appendLine("        maven(\"https://maven.neoforged.net/releases/\")")
        appendLine("    }")
        appendLine("}")
        appendLine()
        append("rootProject.name = \"${plan.modId}\"")
        appendLine()
    }

    private fun gradleProperties(loader: McLoader): String = buildString {
        append("modkit.loader=${loader.name.lowercase()}")
        appendLine()
    }

    private fun build(plan: ScaffoldPlan): String {
        val node = plan.nodes.single()
        return buildString {
            appendLine("plugins {")
            appendLine("    id(\"com.oliveryasuna.modkit.loaders\")")
            ModuleBlocks.pluginIds(plan).forEach { appendLine(it) }
            appendLine("}")
            appendLine()
            appendLine("modkit {")
            appendLine("    modId.set(\"${plan.modId}\")")
            appendLine("    group.set(\"${plan.group}\")")
            appendLine("    version.set(\"${plan.version}\")")
            appendLine()
            appendLine("    minecraft(\"${node.minecraft}\") {")
            appendLine("        loaders.add(com.oliveryasuna.modkit.core.extension.McLoader.${node.loader.name})")
            appendLine("    }")
            val blocks = ModuleBlocks.render(plan)
            if(blocks.isNotEmpty()) {
                appendLine()
                blocks.forEach { appendLine(it) }
            }
            append("}")
            appendLine()
        }
    }
}

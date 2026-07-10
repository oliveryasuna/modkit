package com.oliveryasuna.modkit.scaffold.render

import com.oliveryasuna.modkit.scaffold.ScaffoldPlan

/**
 * Renders the multi-node project shape: the proven single-mod-at-root
 * Stonecutter layout from the `multiversion` module. Emits a settings script
 * that applies the `multiversion.settings` plugin and declares the matrix via
 * `modkitVersions { root { version(...) } }`, the root Stonecutter controller,
 * a central `build.gradle.kts` applying the `multiversion` project plugin, and
 * the shared example sources.
 *
 * In this shape the loader comes from the node name (the settings plugin's
 * `beforeProject` bridge sets `modkit.loader` per node), so there is no
 * `gradle.properties` loader and no base `minecraft(){}` in the central script.
 */
internal object MultiversionRenderer {

    fun render(plan: ScaffoldPlan): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()

        files += GeneratedFile("settings.gradle.kts", settings(plan))
        files += GeneratedFile("stonecutter.gradle.kts", stonecutter(plan))
        files += GeneratedFile("build.gradle.kts", build(plan))
        files += SourceRenderer.render(plan)

        return files
    }

    private fun settings(plan: ScaffoldPlan): String = buildString {
        appendLine("import com.oliveryasuna.modkit.core.extension.McLoader")
        appendLine()
        appendLine("pluginManagement {")
        appendLine("    repositories {")
        appendLine("        gradlePluginPortal()")
        appendLine("        mavenCentral()")
        appendLine("        maven(\"https://maven.fabricmc.net/\")")
        appendLine("        maven(\"https://maven.neoforged.net/releases/\")")
        appendLine("        maven(\"https://maven.kikugie.dev/releases\")")
        appendLine("        maven(\"https://maven.kikugie.dev/snapshots\")")
        appendLine("    }")
        appendLine("}")
        appendLine()
        appendLine("plugins {")
        appendLine("    id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"1.0.0\"")
        appendLine("    id(\"com.oliveryasuna.modkit.multiversion.settings\")")
        appendLine("}")
        appendLine()
        appendLine("dependencyResolutionManagement {")
        appendLine("    repositories {")
        appendLine("        mavenCentral()")
        appendLine("        maven(\"https://maven.fabricmc.net/\")")
        appendLine("        maven(\"https://maven.neoforged.net/releases/\")")
        appendLine("        maven(\"https://maven.kikugie.dev/releases\")")
        appendLine("        maven(\"https://maven.kikugie.dev/snapshots\")")
        appendLine("    }")
        appendLine("}")
        appendLine()
        appendLine("modkitVersions {")
        appendLine("    root {")
        // Group loaders per distinct Minecraft version.
        plan.minecraftVersions.forEach { mc ->
            val loaders = plan.nodes.filter { it.minecraft == mc }.map { it.loader }
            val args = loaders.joinToString(", ") { "McLoader.${it.name}" }
            appendLine("        version(\"$mc\", $args)")
        }
        appendLine("    }")
        appendLine("}")
        appendLine()
        append("rootProject.name = \"${plan.modId}\"")
        appendLine()
    }

    private fun stonecutter(plan: ScaffoldPlan): String = buildString {
        appendLine("plugins {")
        appendLine("    id(\"dev.kikugie.stonecutter\")")
        appendLine("}")
        appendLine()
        append("stonecutter active \"${plan.nodes.first().nodeId}\"")
        appendLine()
    }

    private fun build(plan: ScaffoldPlan): String = buildString {
        appendLine("plugins {")
        appendLine("    id(\"com.oliveryasuna.modkit.multiversion\")")
        ModuleBlocks.pluginIds(plan).forEach { appendLine(it) }
        appendLine("}")
        appendLine()
        appendLine("modkit {")
        appendLine("    modId.set(\"${plan.modId}\")")
        appendLine("    group.set(\"${plan.group}\")")
        appendLine("    version.set(\"${plan.version}\")")
        appendLine()
        appendLine("    multiversion {")
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

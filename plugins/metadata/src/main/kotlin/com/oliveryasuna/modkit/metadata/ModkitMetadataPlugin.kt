package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import com.oliveryasuna.modkit.plugin.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider

public class ModkitMetadataPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `metadata` builds on the shared model — apply core first so `modkit`
        // exists, then attach the `metadata` block as its ExtensionAware child.
        val modkit = project.applyModkitCore()
        val metadata = modkit.registerBlock("metadata", MetadataSpec::class.java)

        metadata.environment.convention("*")
        metadata.validation.failOnMissingIcon.convention(true)
        metadata.validation.failOnInvalidSemver.convention(true)
        metadata.validation.failOnUndeclaredMixinConfig.convention(true)
        metadata.validation.failOnMissingLicense.convention(true)

        // Choose the target loader eagerly from `modkit.loader` — only one
        // manifest is generated per invocation, for the active loader.
        val activeLoader = project.activeLoader()

        registerGenerate(project, modkit, metadata, activeLoader)
        registerValidation(project, modkit, metadata, activeLoader)
    }

    private fun registerGenerate(
        project: Project,
        modkit: ModkitExtension,
        metadata: MetadataSpec,
        activeLoader: McLoader?
    ) {
        if(activeLoader == null) return

        // Minecraft version of the first enabled target that includes the
        // active loader; absent when no such target is declared.
        val minecraftVersion: Provider<String> = project.provider {
            modkit.targets
                .firstOrNull { it.enabled.get() && it.loaders.get().contains(activeLoader) }
                ?.minecraftVersion
        }

        val outputDir = project.layout.buildDirectory.dir("modkit/metadata/${activeLoader.name.lowercase()}")

        val generateTask: TaskProvider<out GenerateManifestTask> = when(activeLoader) {
            McLoader.FABRIC ->
                project.tasks.register("generateFabricModJson", GenerateFabricModJsonTask::class.java) { task ->
                    task.group = "modkit"
                    task.description = "Generates fabric.mod.json from the Modkit model."
                    configureIdentity(task, modkit, metadata, minecraftVersion)
                    task.rawOverrides.set(metadata.fabric.raw)
                    task.mixinConfigs.set(project.modkitManifestContributions().mixinConfigs)
                    task.fabricDatagenEntrypoints.set(project.modkitManifestContributions().fabricDatagenEntrypoints)
                    task.outputDir.set(outputDir)
                }

            McLoader.NEOFORGE ->
                project.tasks.register("generateNeoForgeToml", GenerateNeoForgeTomlTask::class.java) { task ->
                    task.group = "modkit"
                    task.description = "Generates neoforge.mods.toml from the Modkit model."
                    configureIdentity(task, modkit, metadata, minecraftVersion)
                    task.rawOverrides.set(metadata.neoforge.raw)
                    task.mixinConfigs.set(project.modkitManifestContributions().mixinConfigs)
                    task.fabricDatagenEntrypoints.set(project.modkitManifestContributions().fabricDatagenEntrypoints)
                    task.outputDir.set(outputDir)
                }
        }

        // The manifest is common -> main source set. Add the gen dir as a
        // resource source so it is packaged, and wire processResources to the
        // generate task EXPLICITLY: the provider-based srcDir dependency is not
        // reliably inferred once a loader base (Loom/MDG) rewires the resource
        // pipeline, which would otherwise ship a jar with no manifest.
        project.pluginManager.withPlugin("java-base") {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val main = sourceSets.getByName("main")
            main.resources.srcDir(generateTask.flatMap { it.outputDir })
            project.tasks.named(main.processResourcesTaskName) { it.dependsOn(generateTask) }
        }
    }

    private fun configureIdentity(
        task: GenerateManifestTask,
        modkit: ModkitExtension,
        metadata: MetadataSpec,
        minecraftVersion: Provider<String>
    ) {
        task.modId.set(modkit.modId)
        task.version.set(modkit.version)
        task.displayName.set(modkit.displayName)
        task.modDescription.set(modkit.description)
        task.authors.set(modkit.authors)
        task.license.set(modkit.license)
        task.icon.set(metadata.icon)
        task.homepage.set(modkit.urls.homepage)
        task.source.set(modkit.urls.source)
        task.issues.set(modkit.urls.issues)
        task.environment.set(metadata.environment)
        task.minecraftVersion.set(minecraftVersion)
        task.entrypointsMain.set(metadata.entrypoints.main)
        task.entrypointsClient.set(metadata.entrypoints.client)
        task.dependencies.set(metadata.dependsOn.constraints)
    }

    private fun registerValidation(
        project: Project,
        modkit: ModkitExtension,
        metadata: MetadataSpec,
        activeLoader: McLoader?
    ) {
        val validate = project.tasks.register("validateModMetadata", ValidateModMetadataTask::class.java) { task ->
            task.group = "verification"
            task.description = "Validates the resolved mod metadata (semver, icon, license, mixin configs)."

            task.version.set(modkit.version)
            task.icon.set(metadata.icon)
            task.license.set(modkit.license)
            task.neoForgeActive.set(activeLoader == McLoader.NEOFORGE)
            task.resourcesDir.set(project.layout.projectDirectory.dir("src/main/resources"))
            task.failOnMissingIcon.set(metadata.validation.failOnMissingIcon)
            task.failOnInvalidSemver.set(metadata.validation.failOnInvalidSemver)
            task.failOnUndeclaredMixinConfig.set(metadata.validation.failOnUndeclaredMixinConfig)
            task.failOnMissingLicense.set(metadata.validation.failOnMissingLicense)
        }

        // Attach to `check` only where a lifecycle exists.
        project.wireIntoCheck(validate)
    }

}

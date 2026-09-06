package com.oliveryasuna.modkit.metadata.task

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.metadata.extension.DepConstraint
import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import com.oliveryasuna.modkit.metadata.manifest.ManifestInputs
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.*
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.work.DisableCachingByDefault

/**
 * The shared identity and inputs behind the per-loader generate tasks.
 *
 * It holds the resolved model as plain task inputs (so Gradle tracks them and
 * the config cache is happy) and snapshots them into a provider-free
 * [ManifestInputs] for the pure builders. [bindModel] is how the plugin wires
 * it up from the model; subclasses only add their `@TaskAction` and pick a
 * builder.
 */
@DisableCachingByDefault(because = "Abstract base; the concrete subtypes declare their own caching.")
internal abstract class GenerateManifestTask : DefaultTask() {

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val modGroup: Property<String>

    @get:Input
    abstract val displayName: Property<String>

    @get:[Input Optional]
    abstract val modDescription: Property<String>

    @get:Input
    abstract val authors: ListProperty<String>

    @get:[Input Optional]
    abstract val license: Property<String>

    @get:[Input Optional]
    abstract val icon: Property<String>

    @get:[Input Optional]
    abstract val homepage: Property<String>

    @get:[Input Optional]
    abstract val source: Property<String>

    @get:[Input Optional]
    abstract val issues: Property<String>

    @get:Input
    abstract val environment: Property<String>

    @get:[Input Optional]
    abstract val minecraftVersion: Property<String>

    @get:Input
    abstract val entrypointsMain: ListProperty<String>

    @get:Input
    abstract val entrypointsClient: ListProperty<String>

    @get:Input
    abstract val dependencies: MapProperty<String, DepConstraint>

    @get:Input
    abstract val mixinConfigs: SetProperty<String>

    @get:Input
    abstract val fabricDatagenEntrypoints: SetProperty<String>

    @get:Input
    abstract val rawOverrides: MapProperty<String, Any>

    @get:Input
    abstract val substituteTokens: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * Wires the shared identity inputs from the model. Per-loader inputs
     * (`rawOverrides`, the registry contributions, `outputDir`) are set by the
     * plugin, since they differ between Fabric and NeoForge.
     */
    fun bindModel(
        model: ModkitExtension,
        metadata: MetadataSpec,
        minecraftVersion: Provider<String>
    ) {
        modId.set(model.modId)
        version.set(model.version)
        modGroup.set(model.group)
        displayName.set(model.displayName)
        modDescription.set(model.description)
        authors.set(model.authors)
        license.set(model.license)
        icon.set(metadata.icon)
        homepage.set(model.urls.homepage)
        source.set(model.urls.source)
        issues.set(model.urls.issues)
        environment.set(metadata.environment)
        this.minecraftVersion.set(minecraftVersion)
        entrypointsMain.set(metadata.entrypoints.main)
        entrypointsClient.set(metadata.entrypoints.client)
        dependencies.set(metadata.dependsOn.constraints)
        substituteTokens.set(metadata.substituteTokens)
    }

    /**
     * Snapshots the live inputs into a plain [ManifestInputs] for the builders.
     */
    protected fun resolveInputs(): ManifestInputs =
        ManifestInputs(
            modId = modId.get(),
            version = version.get(),
            group = modGroup.get(),
            displayName = displayName.get(),
            description = modDescription.orNull,
            authors = authors.get(),
            license = license.orNull,
            icon = icon.orNull,
            homepage = homepage.orNull,
            source = source.orNull,
            issues = issues.orNull,
            environment = environment.get(),
            minecraftVersion = minecraftVersion.orNull,
            entrypointsMain = entrypointsMain.get(),
            entrypointsClient = entrypointsClient.get(),
            dependencies = dependencies.get(),
            // Sorted so the manifest is stable regardless of registration
            // order.
            mixinConfigs = mixinConfigs.get().sorted(),
            fabricDatagenEntrypoints = fabricDatagenEntrypoints.get().sorted(),
            rawOverrides = rawOverrides.get(),
            substituteTokens = substituteTokens.get(),
        )
}

package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.metadata.extension.DepConstraint
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.work.DisableCachingByDefault

/**
 * Shared identity/inputs for the per-loader generate tasks. Holds the resolved
 * model as plain task inputs and snapshots them into a provider-free
 * [ManifestInputs] for the pure builders.
 */
@DisableCachingByDefault(because = "Abstract base; concrete subtypes declare their own caching behavior.")
internal abstract class GenerateManifestTask : DefaultTask() {

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    abstract val version: Property<String>

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

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    protected fun resolveInputs(): ManifestInputs =
        ManifestInputs(
            modId = modId.get(),
            version = version.get(),
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
            mixinConfigs = mixinConfigs.get().sorted(),
            fabricDatagenEntrypoints = fabricDatagenEntrypoints.get().sorted(),
            rawOverrides = rawOverrides.get()
        )

}

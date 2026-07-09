package com.oliveryasuna.modkit.ci

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.work.DisableCachingByDefault

/**
 * Shared inputs for the generate/verify workflow tasks. Holds the resolved
 * model as plain task inputs and snapshots them into a provider-free
 * [CiWorkflowInputs] for the pure builder.
 */
@DisableCachingByDefault(because = "Abstract base; concrete subtypes declare their own caching behavior.")
internal abstract class CiWorkflowTask : DefaultTask() {

    @get:Input
    abstract val provider: Property<String>

    @get:Input
    abstract val matrix: ListProperty<CiMatrixEntry>

    @get:Input
    abstract val java: Property<Int>

    @get:Input
    abstract val cache: Property<Boolean>

    @get:Input
    abstract val publishOnTag: Property<Boolean>

    @get:Input
    abstract val publishApplied: Property<Boolean>

    @get:Input
    abstract val modrinthSecret: Property<String>

    @get:Input
    abstract val curseforgeSecret: Property<String>

    @get:Input
    abstract val githubSecret: Property<String>

    protected fun resolveInputs(): CiWorkflowInputs {
        val providerValue = provider.get()
        if(providerValue != "github") {
            throw GradleException("Unsupported CI provider '$providerValue'; only 'github' is supported.")
        }

        return CiWorkflowInputs(
            matrix = matrix.get(),
            java = java.get(),
            cache = cache.get(),
            publishOnTag = publishOnTag.get(),
            publishApplied = publishApplied.get(),
            modrinthSecret = modrinthSecret.get(),
            curseforgeSecret = curseforgeSecret.get(),
            githubSecret = githubSecret.get()
        )
    }

}

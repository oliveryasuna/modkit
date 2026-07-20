package com.oliveryasuna.modkit.metadata

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

/**
 * Validates the resolved metadata model (semver, declared icon, mixin configs).
 */
@DisableCachingByDefault(because = "Validation is fast and produces no cacheable output.")
internal abstract class ValidateModMetadataTask : DefaultTask() {

    @get:[Input Optional]
    abstract val version: Property<String>

    @get:[Input Optional]
    abstract val icon: Property<String>

    @get:[Input Optional]
    abstract val license: Property<String>

    /**
     * The common source set's resource roots. Resolved from the source set (not
     * from `projectDirectory`) so it holds under Stonecutter, where the built
     * node's shared sources live outside the node's own directory, and picks up
     * the generated-manifest dir that is added as a resource source.
     */
    @get:[InputFiles Optional PathSensitive(PathSensitivity.RELATIVE)]
    abstract val resourceRoots: ConfigurableFileCollection

    @get:Input
    abstract val neoForgeActive: Property<Boolean>

    @get:Input
    abstract val failOnMissingIcon: Property<Boolean>

    @get:Input
    abstract val failOnInvalidSemver: Property<Boolean>

    @get:Input
    abstract val failOnUndeclaredMixinConfig: Property<Boolean>

    @get:Input
    abstract val failOnMissingLicense: Property<Boolean>

    @TaskAction
    fun validate() {
        val iconName = icon.orNull
        val iconExists = iconName != null && resourceRoots.files.any { root ->
            root.resolve(iconName).exists()
        }

        val errors = ModMetadataValidator.validate(
            version = version.orNull,
            icon = iconName,
            iconExists = iconExists,
            license = license.orNull,
            isNeoForge = neoForgeActive.get(),
            failOnMissingIcon = failOnMissingIcon.get(),
            failOnInvalidSemver = failOnInvalidSemver.get(),
            failOnUndeclaredMixinConfig = failOnUndeclaredMixinConfig.get(),
            failOnMissingLicense = failOnMissingLicense.get()
        )

        if(errors.isNotEmpty()) {
            val report = buildString {
                append("Invalid mod metadata:")
                errors.forEach { append("\n  - ").append(it) }
            }
            throw GradleException(report)
        }
    }

}

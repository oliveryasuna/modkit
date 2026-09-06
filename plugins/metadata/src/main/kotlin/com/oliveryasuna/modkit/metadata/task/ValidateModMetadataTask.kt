package com.oliveryasuna.modkit.metadata.task

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.metadata.ModMetadataValidator
import com.oliveryasuna.modkit.metadata.extension.MetadataSpec
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

/**
 * Checks the resolved metadata (semver, a declared-but-missing icon, and the
 * NeoForge license rule) and fails the build with a combined report if anything
 * is wrong. The rules themselves are in [ModMetadataValidator]; this only
 * gathers inputs and decides the icon actually exists.
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
     * The common source set's resource roots. Taken from the source set (not a
     * `projectDirectory`-relative guess) so it holds under Stonecutter, where a
     * built node's shared sources live outside the node's own directory, and so it
     * sees the generated-manifest dir added as a resource source.
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

    fun bindModel(
        model: ModkitExtension,
        metadata: MetadataSpec,
        neoForgeActive: Boolean
    ) {
        version.set(model.version)
        icon.set(metadata.icon)
        license.set(model.license)
        this.neoForgeActive.set(neoForgeActive)
        failOnMissingIcon.set(metadata.validation.failOnMissingIcon)
        failOnInvalidSemver.set(metadata.validation.failOnInvalidSemver)
        failOnUndeclaredMixinConfig.set(metadata.validation.failOnUndeclaredMixinConfig)
        failOnMissingLicense.set(metadata.validation.failOnMissingLicense)
    }

    @TaskAction
    fun validate() {
        val iconName = icon.orNull
        val iconExists = iconName != null && resourceRoots.files.any { root -> root.resolve(iconName).exists() }

        val errors = ModMetadataValidator.validate(
            version = version.orNull,
            icon = iconName,
            iconExists = iconExists,
            license = license.orNull,
            isNeoForge = neoForgeActive.get(),
            failOnMissingIcon = failOnMissingIcon.get(),
            failOnInvalidSemver = failOnInvalidSemver.get(),
            failOnUndeclaredMixinConfig = failOnUndeclaredMixinConfig.get(),
            failOnMissingLicense = failOnMissingLicense.get(),
        )

        if(errors.isEmpty()) return

        val report = buildString {
            append("Invalid mod metadata:")
            errors.forEach { append("\n  - ").append(it) }
        }
        throw GradleException(report)
    }

}

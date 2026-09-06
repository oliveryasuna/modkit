package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.metadata.task.ValidateModMetadataTask
import com.oliveryasuna.modkit.plugin.PluginFeature
import com.oliveryasuna.modkit.plugin.wireIntoCheck
import com.oliveryasuna.modkit.plugin.withCommonSourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Registers `validateModMetadata` and hangs it off `check` where a lifecycle
 * exists.
 */
internal object MetadataValidation : PluginFeature<MetadataContext> {

    private const val VALIDATE_TASK_NAME: String = "validateModMetadata"

    override fun install(ctx: MetadataContext) {
        val validate = ctx.project.tasks.register(VALIDATE_TASK_NAME, ValidateModMetadataTask::class.java) { task ->
            task.group = LifecycleBasePlugin.VERIFICATION_GROUP
            task.description = "Validates the resolved mod metadata (semver, icon, license, mixin configs)."
            task.bindModel(ctx.model, ctx.metadata, neoForgeActive = ctx.activeLoader == ModLoader.NEOFORGE)
        }

        // Resolve the icon against the common source set's live resource roots
        // (which include the generated-manifest dir, and shared sources outside
        // this node under Stonecutter) rather than a projectDir-relative guess.
        ctx.project.withCommonSourceSet(ctx.commonSourceSet) { common ->
            validate.configure { it.resourceRoots.from(common.resources.sourceDirectories) }
        }

        ctx.project.wireIntoCheck(validate)
    }

}

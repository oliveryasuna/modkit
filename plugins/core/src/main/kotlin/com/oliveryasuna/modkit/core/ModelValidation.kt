package com.oliveryasuna.modkit.core

import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.MODKIT_TASK_PREFIX
import com.oliveryasuna.modkit.plugin.PluginFeature
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

private const val VALIDATE_TASK_NAME: String = "${MODKIT_TASK_PREFIX}ValidateModel"

/**
 * Registers `modkitValidateModel` and, wherever a lifecycle exists, hangs it
 * off `check`.
 *
 * The rules themselves live in [ModkitModelValidator], which is pure and knows
 * nothing about Gradle. All this feature does is snapshot the model into plain
 * values and decide what to do with whatever comes back.
 */
internal object ModelValidation : PluginFeature {

    // Strict fails the build; lenient only warns. No public switch for now: a
    // broken model is a mistake worth stopping for.
    private const val STRICT: Boolean = true

    override fun install(
        project: Project,
        model: ModkitExtension
    ) {
        val validate = project.tasks.register(VALIDATE_TASK_NAME) { task ->
            task.group = LifecycleBasePlugin.VERIFICATION_GROUP
            task.description = "Validates the resolved Modkit model (modId, targets, loaders)."

            // Snapshot everything the action needs, so at execution time it
            // touches neither the model nor the project.
            val strict = STRICT
            val modId = model.modId
            val targets = model.targets.map { target ->
                ModkitModelValidator.TargetView(target.name, target.loaders.get().isNotEmpty())
            }

            task.doLast {
                val errors = ModkitModelValidator.validate(modId.orNull, targets)
                if(errors.isEmpty()) {
                    return@doLast
                }

                val report = buildString {
                    append("Invalid Modkit model:")
                    errors.forEach { append("\n  - ").append(it) }
                }
                if(strict) {
                    throw GradleException(report)
                } else {
                    task.logger.warn(report)
                }
            }
        }

        // If something brings a `check` task (the base plugin, the Java plugin,
        // and so on), run validation as part of it. That is what stops a broken
        // model from slipping through a normal build.
        project.plugins.withType(LifecycleBasePlugin::class.java) {
            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { check ->
                check.dependsOn(validate)
            }
        }
    }

}

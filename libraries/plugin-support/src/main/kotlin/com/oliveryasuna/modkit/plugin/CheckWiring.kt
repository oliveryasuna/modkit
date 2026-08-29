package com.oliveryasuna.modkit.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Makes `check` depend on [task], but only once a lifecycle is around to have a
 * `check` in the firs tplace.,
 *
 * Reacting to [LifecycleBasePlugin], rather than hardcoding the
 * `"check"`/`"lifecycle-base"` strings keeps it honest: some projects (the
 * `c ore` plugin included) never apply a lifecycle, and there this simply never
 * fires.
 */
public fun Project.wireIntoCheck(task: TaskProvider<out Task>) {
    plugins.withType(LifecycleBasePlugin::class.java) {
        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { it.dependsOn(task) }
    }
}

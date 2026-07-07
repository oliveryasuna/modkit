package com.oliveryasuna.modkit.example

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Minimal example plugin. Registers an [ExampleExtension] and a `greet` task
 * that prints the configured message.
 */
public class ExamplePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("example", ExampleExtension::class.java)
        extension.message.convention("Hello from modkit!")

        target.tasks.register("greet", GreetTask::class.java) { task ->
            task.group = "modkit"
            task.description = "Prints the configured example message."
            task.message.set(extension.message)
        }
    }
}

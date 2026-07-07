package com.oliveryasuna.modkit.example

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Prints [message] to the console. */
@DisableCachingByDefault(because = "Trivial console output; not worth caching.")
public abstract class GreetTask : DefaultTask() {

    @get:Input
    public abstract val message: Property<String>

    @TaskAction
    public fun greet() {
        logger.lifecycle(message.get())
    }
}

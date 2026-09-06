package com.oliveryasuna.modkit.plugin

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Shorthand for the project's source sets.
 */
public val Project.sourceSets: SourceSetContainer
    get() = extensions.getByType(SourceSetContainer::class.java)

/**
 * Runs [action] against the named source set.
 */
public fun Project.withCommonSourceSet(
    name: String,
    action: (SourceSet) -> Unit
) {
    plugins.withType(JavaBasePlugin::class.java) {
        action(extensions.getByType(SourceSetContainer::class.java).getByName(name))
    }
}

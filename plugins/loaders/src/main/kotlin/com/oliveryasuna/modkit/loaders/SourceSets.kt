package com.oliveryasuna.modkit.loaders

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * Shorthand for the project's source sets. Both bases reach for these a lot.
 */
internal val Project.sourceSets: SourceSetContainer
    get() = extensions.getByType(SourceSetContainer::class.java)

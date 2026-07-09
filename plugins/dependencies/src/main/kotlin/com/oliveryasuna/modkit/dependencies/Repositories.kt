package com.oliveryasuna.modkit.dependencies

import org.gradle.api.Project

/**
 * Adds the Modrinth Maven, scoped to `maven.modrinth:*` so it is only consulted
 * for its own artifacts. Usage: `mod("maven.modrinth:<project>:<version>")`.
 */
internal fun Project.addModrinthRepository() {
    repositories.exclusiveContent { exclusive ->
        exclusive.forRepository {
            repositories.maven { repo ->
                repo.name = "Modrinth"
                repo.setUrl("https://api.modrinth.com/maven")
            }
        }
        exclusive.filter { it.includeGroup("maven.modrinth") }
    }
}

/**
 * Adds CurseMaven, scoped to `curse.maven:*`. Usage:
 * `mod("curse.maven:<slug>-<projectId>:<fileId>")`.
 */
internal fun Project.addCurseMavenRepository() {
    repositories.exclusiveContent { exclusive ->
        exclusive.forRepository {
            repositories.maven { repo ->
                repo.name = "CurseMaven"
                repo.setUrl("https://cursemaven.com")
            }
        }
        exclusive.filter { it.includeGroup("curse.maven") }
    }
}

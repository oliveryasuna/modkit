package com.oliveryasuna.modkit.publish

import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Resolves the changelog from a `source` string:
 *
 *  - `git` runs Git at query time via [GitChangelogValueSource].
 *  - `file:PATH` reads the text of the given (project-relative) file.
 *  - anything else is treated as a literal changelog string.
 *
 * The result is a lazy provider; none of these read the filesystem or shell out
 * until the value is queried.
 */
internal object Changelogs {

    private const val FILE_PREFIX: String = "file:"

    fun provider(project: Project, source: Provider<String>): Provider<String> =
        source.flatMap { raw ->
            when {
                raw == "git" ->
                    project.providers.of(GitChangelogValueSource::class.java) { spec ->
                        spec.parameters.projectDir.set(project.layout.projectDirectory)
                    }

                raw.startsWith(FILE_PREFIX) ->
                    project.providers.fileContents(
                        project.layout.projectDirectory.file(raw.removePrefix(FILE_PREFIX))
                    ).asText

                else ->
                    project.provider { raw }
            }
        }

}

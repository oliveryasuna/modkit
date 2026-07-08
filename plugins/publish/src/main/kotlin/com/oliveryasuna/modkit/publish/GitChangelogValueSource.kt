package com.oliveryasuna.modkit.publish

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Builds a changelog from the project's Git history at query time. Runs `git`
 * through [ExecOperations] so the work happens lazily (only when the changelog
 * value is actually needed) and stays configuration-cache safe.
 *
 * The changelog is the subject line of every commit since the last tag, or of
 * the entire history when the repository has no tags.
 */
internal abstract class GitChangelogValueSource @Inject constructor(
    private val execOperations: ExecOperations
) : ValueSource<String, GitChangelogValueSource.Params> {

    interface Params : ValueSourceParameters {

        val projectDir: DirectoryProperty

    }

    override fun obtain(): String {
        val dir = parameters.projectDir.get().asFile

        val lastTag = git(dir, "describe", "--tags", "--abbrev=0")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val log =
            if(lastTag != null) git(dir, "log", "$lastTag..HEAD", "--pretty=format:%s")
            else git(dir, "log", "--pretty=format:%s")

        return log?.trim().orEmpty()
    }

    private fun git(dir: File, vararg args: String): String? {
        val stdout = ByteArrayOutputStream()
        val result = execOperations.exec { spec ->
            spec.workingDir = dir
            spec.commandLine(listOf("git") + args)
            spec.standardOutput = stdout
            spec.errorOutput = ByteArrayOutputStream()
            spec.isIgnoreExitValue = true
        }
        return if(result.exitValue == 0) stdout.toString(Charsets.UTF_8.name()) else null
    }

}

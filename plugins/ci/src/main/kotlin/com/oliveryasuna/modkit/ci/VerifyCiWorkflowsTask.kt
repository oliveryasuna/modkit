package com.oliveryasuna.modkit.ci

import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Regenerates the workflow in memory and compares it against the committed
 * `.github/workflows/ci.yml`, failing with the drift when they differ or the
 * file is missing.
 */
@DisableCachingByDefault(because = "Verification is fast and produces no cacheable output.")
internal abstract class VerifyCiWorkflowsTask : CiWorkflowTask() {

    @get:Internal
    abstract val committedFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val file = committedFile.get().asFile
        val expected = CiWorkflowBuilder.build(resolveInputs())

        if(!file.exists()) {
            throw GradleException("CI workflow is missing at ${file.path}. Run generateCiWorkflows to create it.")
        }

        val actual = file.readText()
        if(actual != expected) {
            throw GradleException(buildDriftReport(expected, actual))
        }
    }

    private fun buildDriftReport(expected: String, actual: String): String {
        val expectedLines = expected.lines()
        val actualLines = actual.lines()
        return buildString {
            append("CI workflow is out of date; run generateCiWorkflows. Drift:")
            val lineCount = maxOf(expectedLines.size, actualLines.size)
            for(i in 0 until lineCount) {
                val expectedLine = expectedLines.getOrNull(i)
                val actualLine = actualLines.getOrNull(i)
                if(expectedLine != actualLine) {
                    append("\n  line ${i + 1}:")
                    append("\n    expected: ${expectedLine ?: "<none>"}")
                    append("\n    actual:   ${actualLine ?: "<none>"}")
                }
            }
        }
    }

}

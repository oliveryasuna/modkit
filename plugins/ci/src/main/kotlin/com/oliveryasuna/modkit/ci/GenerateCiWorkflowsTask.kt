package com.oliveryasuna.modkit.ci

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes the GitHub Actions workflow to the committed `.github/workflows/ci.yml`
 */
@CacheableTask
internal abstract class GenerateCiWorkflowsTask : CiWorkflowTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(CiWorkflowBuilder.build(resolveInputs()))
    }

}

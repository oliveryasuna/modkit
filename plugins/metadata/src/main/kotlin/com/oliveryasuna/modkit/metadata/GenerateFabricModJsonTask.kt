package com.oliveryasuna.modkit.metadata

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction

/** Writes `fabric.mod.json` at the output directory root. */
@CacheableTask
internal abstract class GenerateFabricModJsonTask : GenerateManifestTask() {

    @TaskAction
    fun generate() {
        val file = outputDir.get().asFile.resolve("fabric.mod.json")
        file.parentFile.mkdirs()
        file.writeText(ManifestBuilders.buildFabricModJson(resolveInputs()))
    }

}

package com.oliveryasuna.modkit.metadata.task

import com.oliveryasuna.modkit.metadata.manifest.FabricModJson
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class GenerateFabricModJsonTask : GenerateManifestTask() {

    @TaskAction
    fun generate() {
        val file = outputDir.get().asFile.resolve("fabric.mod.json")
        file.parentFile.mkdirs()
        file.writeText(FabricModJson.build(resolveInputs()))
    }

}

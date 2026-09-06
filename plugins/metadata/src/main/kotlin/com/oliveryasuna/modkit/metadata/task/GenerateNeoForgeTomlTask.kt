package com.oliveryasuna.modkit.metadata.task

import com.oliveryasuna.modkit.metadata.manifest.NeoForgeModsToml
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction

/** Writes `META-INF/neoforge.mods.toml` under the output directory. */
@CacheableTask
internal abstract class GenerateNeoForgeTomlTask : GenerateManifestTask() {

    @TaskAction
    fun generate() {
        val file = outputDir.get().asFile.resolve("META-INF/neoforge.mods.toml")
        file.parentFile.mkdirs()
        file.writeText(NeoForgeModsToml.build(resolveInputs()))
    }

}

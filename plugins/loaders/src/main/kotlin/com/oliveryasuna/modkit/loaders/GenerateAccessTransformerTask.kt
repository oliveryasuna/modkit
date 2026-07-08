package com.oliveryasuna.modkit.loaders

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

/**
 * Generates a NeoForge access transformer from the project's access wideners
 * (Modkit's write-once feature). Pure function of its inputs → cacheable and
 * configuration-cache safe.
 */
@CacheableTask
public abstract class GenerateAccessTransformerTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val accessWideners: ConfigurableFileCollection

    @get:OutputFile
    public abstract val accessTransformer: RegularFileProperty

    @TaskAction
    public fun generate() {
        val contents = accessWideners.files.map { it.readText() }
        val output = accessTransformer.get().asFile
        output.parentFile.mkdirs()
        output.writeText(AwToAt.convert(contents))
    }

}

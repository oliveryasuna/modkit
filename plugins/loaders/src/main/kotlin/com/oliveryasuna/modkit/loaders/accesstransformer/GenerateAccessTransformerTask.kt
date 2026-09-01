package com.oliveryasuna.modkit.loaders.accesstransformer

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

/**
 * TODO: Document.
 */
@CacheableTask
public abstract class GenerateAccessTransformerTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val accessWideners: ConfigurableFileCollection

    @get:OutputFile
    public abstract val accessTransformer: RegularFileProperty

    @TaskAction
    public fun generate() {
        // TODO: Implement.
    }

}
